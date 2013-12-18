package se.skltp.nt.intsvc;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.module.xml.stax.ReversibleXMLStreamReader;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.XmlUtil;
import se.skltp.nt.svc.ConfigProperties;
import se.skltp.nt.svc.StringUtil;

/**
 * Analyze the message and store extracted data in outbound properties.
 * <p/>
 * <ul>
 * <li>NT_SERVICE_CONTRACT_URI: the service contract (namespace for first node)</li>
 * <li>NT_SERVICE_CONTRACT_ACTION: the action (first node name)</li>
 * <li>NT_SERVICE_DOMAIN: for filtering</li>
 * <li>NT_CATEGORIZATION: for filtering</li>
 * <li>NT_RIVTA_VERSION</li>
 * <li>NT_SOAP_ACTION: uri + ":" + action</li>
 * </ul>
 * <p/>
 * Filtering data is extracted by finding any serviceDomain of the correct type
 * <p/>
 * Extract NT_RIVTA_VERSION and NT_SERVICE_CONTRACT_URI from a ReversibleXMLStreamReader
 */
public class CreatePropsAndConvertToStringTransformer extends AbstractMessageTransformer {
    // TODO: are there multiple SOAP namespaces?
    private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String NT_NS = "urn:riv:itintegration:notification:1";
    private static final QName QNAME_SERVICE_DOMAIN = new QName(NT_NS, "serviceDomain");
    private static final QName QNAME_CATEGORIZATION = new QName(NT_NS, "categorization");

    private static final Logger log = LoggerFactory.getLogger(CreatePropsAndConvertToStringTransformer.class);

    // spring injection
    private ConfigProperties configProperties;

    public void setConfigProperties(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {

        ReversibleXMLStreamReader reader = (ReversibleXMLStreamReader) message.getPayload();

        try {
            // extra the data, reset the reader and convert the payload to a string representation
            Extractor extractor = new Extractor(reader).parse();
            reader.reset();
            message.setPayload(XmlUtil.convertXMLStreamReaderToString(reader, encoding));

            handleExtractedProperties(message, extractor);
            handleEndpointAddress(message, extractor);

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    private void handleExtractedProperties(MuleMessage message, Extractor extractor) {
        if ( log.isDebugEnabled() ) log.debug("Extracted tk '" + extractor.getServiceContractUri() +
                "', action '" + extractor.getServiceContractAction() +
                "', serviceDomain '" + extractor.getServiceDomain() +
                "', categorization '" + extractor.getCategorization() +
                "', rivtaVersion '" + extractor.getRivtaVersion() + "'");

        message.setOutboundProperty("NT_SERVICE_CONTRACT_URI", extractor.getServiceContractUri());
        message.setOutboundProperty("NT_SERVICE_CONTRACT_ACTION", extractor.getServiceContractAction());
        message.setOutboundProperty("NT_SERVICE_DOMAIN", extractor.getServiceDomain());
        message.setOutboundProperty("NT_CATEGORIZATION", extractor.getCategorization());
        message.setOutboundProperty("NT_RIVTA_VERSION", extractor.getRivtaVersion());
        message.setOutboundProperty("NT_SOAP_ACTION", extractor.getSoapAction());
    }

    private void handleEndpointAddress(MuleMessage message, Extractor extractor) {
        // spit VP_BASE_URL into host, port and pathBase. We expect the VP_BASE_URL to match the pattern
        // <http-part>:<host-part>:<port>[/<pathBase-with-no-colons>]
        String url = configProperties.get("VP_BASE_URL");
        String[] parts = url.split(":");
        if ( parts.length != 3 ) {
            throw new IllegalArgumentException("VP_BASE_URL[" + url + "] must contain exactly two ':'!");
        }
        // strip the '//' from the http://
        if (!parts[1].startsWith("//")) {
            throw new IllegalArgumentException("VP_BASE_URL[" + url + "] lacks ://!");
        }
        String host = parts[1].substring(2);
        // now handle port and base
        parts = parts[2].split("/");
        String port = parts[0];
        String pathBase = StringUtil.join(parts, 1, parts.length, "/");

        // build path according to namespace
        String path = pathBase + "/" + createUrlFromServiceContract(extractor);
        if ( log.isDebugEnabled() ) {
            log.debug("host " + host + ", port " + port + ", path " + path);
        }
        message.setOutboundProperty("NT_ENDPOINT_HOST", host);
        message.setOutboundProperty("NT_ENDPOINT_PORT", port);
        message.setOutboundProperty("NT_ENDPOINT_PATH", path);
    }

    private String createUrlFromServiceContract(Extractor extractor) {
        String[] parts = extractor.getServiceContractUri().split(":");
        StringBuilder sb = new StringBuilder();
        // start at 2 to skip urn:riv:
        for ( int i = 2; i < parts.length; i++ ) {
            String part = parts[i];
            if ( sb.length() > 0 ) {
                sb.append("/");
            }
            // the service contract uri adds "Responder" to the name of the service contract
            if ( part.endsWith("Responder") ) {
                part = part.substring(0, part.length() - "Responder".length());
            }
            sb.append(part);
        }
        sb.append("/").append(extractor.getRivtaVersion());
        return sb.toString();
    }


    private class Extractor {
        private ReversibleXMLStreamReader reader;
        private String serviceContractUri;
        private String serviceContractAction;
        private String rivtaVersion;
        private String serviceDomain = "";
        private String categorization = "";

        public Extractor(ReversibleXMLStreamReader reader) throws XMLStreamException {
            this.reader = reader;
        }

        private Extractor parse() throws XMLStreamException {

            boolean inHeader = false;
            boolean inBody = false;

            int event = reader.getEventType();

            while ( reader.hasNext() ) {
                switch (event) {

                    case XMLStreamConstants.START_ELEMENT: {
                        QName qname = reader.getName();
                        String local = qname.getLocalPart();
                        String ns = qname.getNamespaceURI();

                        if ( local.equals("Header") && ns.equals(SOAP_NS) ) {
                            inHeader = true;
                        }

                        if ( local.equals("Body") && ns.equals(SOAP_NS) ) {
                            inBody = true;
                        }

                        if ( inHeader ) {
                            if ( local.equals("To") ) {
                                rivtaVersion = "rivtabp20";
                            }

                            if ( local.equals("LogicalAddress") ) {
                                rivtaVersion = "rivtabp21";
                            }
                        }

                        if ( inBody ) {

                            // the first node should contain the service contract
                            if ( serviceContractUri == null && ns.startsWith("urn:riv") ) {
                                serviceContractUri = ns;
                                serviceContractAction = local;
                            }

                            if ( qname.equals(QNAME_SERVICE_DOMAIN) ) {
                                if ( serviceDomain.length() > 0 ) {
                                    throw new RuntimeException("Found multiple servicedomains, first " + serviceDomain
                                            + ", new " + reader.getElementText());
                                }
                                serviceDomain = reader.getElementText();
                            }

                            if ( qname.equals(QNAME_CATEGORIZATION) ) {
                                if ( categorization.length() > 0 ) {
                                    throw new RuntimeException("Found multiple categorization, first " + categorization
                                            + ", new " + reader.getElementText());
                                }
                                categorization = reader.getElementText();
                            }
                        }

                        break;
                    }

                    case XMLStreamConstants.END_ELEMENT: {
                        QName qname = reader.getName();
                        String local = qname.getLocalPart();
                        String ns = qname.getNamespaceURI();

                        if ( local.equals("Header") && ns.equals(SOAP_NS) ) {
                            inHeader = false;
                        }

                        if ( local.equals("Body") && ns.equals(SOAP_NS) ) {
                            inBody = false;
                        }

                        break;
                    }

                    case XMLStreamConstants.CHARACTERS:
                        break;

                    case XMLStreamConstants.START_DOCUMENT:
                    case XMLStreamConstants.END_DOCUMENT:
                    case XMLStreamConstants.ATTRIBUTE:
                    case XMLStreamConstants.NAMESPACE:
                        break;

                    default:
                        break;
                }
                event = reader.next();
            }

            if ( serviceContractUri == null || rivtaVersion == null ) {
                // TODO: maybe use a better exception? Or will mule dump enough data already?
                throw new RuntimeException("Unable to find service contract or rivtaVersion!");
            }
            return this;
        }

        public String getServiceContractUri() {
            return serviceContractUri;
        }

        public String getRivtaVersion() {
            return rivtaVersion;
        }

        public String getServiceContractAction() {
            return serviceContractAction;
        }

        public String getServiceDomain() {
            return serviceDomain;
        }

        public String getCategorization() {
            return categorization;
        }

        public String getSoapAction() {
            return serviceContractUri + ":" + serviceContractAction;
        }
    }
}

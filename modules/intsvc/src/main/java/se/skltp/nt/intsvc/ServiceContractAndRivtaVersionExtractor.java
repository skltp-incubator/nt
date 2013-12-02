package se.skltp.nt.intsvc;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.module.xml.stax.ReversibleXMLStreamReader;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extract NT_RIVTA_VERSION and NT_SERVICE_CONTRACT_URI from a ReversibleXMLStreamReader
 */
public class ServiceContractAndRivtaVersionExtractor extends AbstractMessageTransformer {

    private static final Logger log = LoggerFactory.getLogger(ServiceContractAndRivtaVersionExtractor.class);

    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {

        ReversibleXMLStreamReader reader = (ReversibleXMLStreamReader) message.getPayload();

        try {
            Extractor extractor = new Extractor(reader).parse();
            reader.reset();
            log.info("found tk " + extractor.getServiceContractUri() + ", rivtaV " + extractor.getRivtaVersion());
            message.setProperty("NT_SERVICE_CONTRACT_URI", extractor.getServiceContractUri(), PropertyScope.INVOCATION);
            message.setProperty("NT_SERVICE_CONTRACT_ACTION", extractor.getServiceContractAction(), PropertyScope.INVOCATION);
            message.setProperty("NT_RIVTA_VERSION", extractor.getRivtaVersion(), PropertyScope.INVOCATION);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    private class Extractor {
        private String serviceContractUri;
        private String rivtaVersion;
        private ReversibleXMLStreamReader reader;
        private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
        private String serviceContractAction;

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
                        String local = reader.getLocalName();
                        String ns = reader.getNamespaceURI();

                        if ( local.equals("Header") && ns.equals(SOAP_NS)) {
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

                            if (ns.startsWith("urn:riv")) {
                                serviceContractUri = ns;
                                serviceContractAction = local;
                                return this; // EXIT HERE!
                            }

                        }

                        break;
                    }

                    case XMLStreamConstants.END_ELEMENT: {
                        String local = reader.getLocalName();
                        String ns = reader.getNamespaceURI();

                        if ( local.equals("Header") && ns.equals(SOAP_NS)) {
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
            // TODO: use better exception (and message)
            throw new RuntimeException("Unable to find body in message!");
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
    }
}

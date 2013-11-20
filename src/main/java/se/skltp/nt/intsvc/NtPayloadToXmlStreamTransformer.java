package se.skltp.nt.intsvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * Transform from an NT-object to a SOAPEnvelope.
 * Not the fastest way of working but wth...
 *
 * @author matsolsson
 */
public class NtPayloadToXmlStreamTransformer extends AbstractMessageTransformer {

    private static final Logger log = LoggerFactory.getLogger(NtPayloadToXmlStreamTransformer.class);
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {

        Object obj = message.getPayload();
        log.info("transforming " + obj);
        NtPayload payload = (NtPayload) obj;

        try {
            String logicalAddress = message.getInvocationProperty("logicalAddress");
            log.info("logAddr " + logicalAddress);
            message.setPayload(createFullEnvelopeXmlStreamPayload(logicalAddress, payload));

            // String baseVpUrl = message.getProperty("NT_VP_BASE_URL", PropertyScope.SESSION);

            String baseVpUrl = "nt-services";
            String url = baseVpUrl + payload.getTrailingPartOfVpUrl();
            message.setProperty("NT_VP_URL", url, PropertyScope.INVOCATION);
            message.setProperty("NT_RIVTA_VERSION", payload.getRivtaVersion(), PropertyScope.INVOCATION);
            message.setProperty("NT_SERVICE_CONTRACT_URI", payload.getServiceContractURI(), PropertyScope.INVOCATION);
            message.setProperty("NT_SOAP_ACTION", payload.getSoapAction(), PropertyScope.INVOCATION);
            message.setProperty("NT_CONTENT_TYPE", "theContentType?", PropertyScope.INVOCATION);

            log.info("endpointUrl " + url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    private XMLStreamReader createFullEnvelopeXmlStreamPayload(String logicalAddress, NtPayload payload) throws javax.xml.transform.TransformerException, SOAPException, IOException, XMLStreamException {
        DOMResult content = getStringAsDom(payload.getServiceContractBody());
        SOAPMessage msg = createMessage(logicalAddress, content);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);
        final StringReader reader = new StringReader(baos.toString());

        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        return inputFactory.createXMLStreamReader(reader);
    }

    private DOMResult getStringAsDom(String text) throws javax.xml.transform.TransformerException {
        Source txtSrc = new StreamSource(new StringReader(text));
        DOMResult result = new DOMResult();
        TRANSFORMER_FACTORY.newTransformer().transform(txtSrc, result);
        return result;
    }

    public static final QName TO_QNAME = new QName("http://www.w3.org/2005/08/addressing", "To");
    public static final QName LOGICAL_ADDRESS_QNAME = new QName("urn:riv:itintegration:registry:1", "LogicalAddress");

    private SOAPMessage createMessage(String logicalAddress, DOMResult content) throws SOAPException {

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage();
        SOAPPart part = msg.getSOAPPart();

        SOAPEnvelope envelope = part.getEnvelope();
        SOAPHeader header = envelope.getHeader();
        SOAPBody body = envelope.getBody();

        // TODO: support rivtabp2.0 as well ...
        SOAPHeaderElement elem = header.addHeaderElement(LOGICAL_ADDRESS_QNAME);
        elem.setValue(logicalAddress);

        body.addDocument((Document) content.getNode());

        return msg;
    }


}

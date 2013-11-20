package se.skltp.nt.intsvc;

import java.io.StringWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mats.olsson@callistaenterprise.se
 */
public class SoapBodyToNtPayloadTransformer extends AbstractMessageTransformer {

    private static final Logger log = LoggerFactory.getLogger(SoapBodyToNtPayloadTransformer.class);
    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {

        Object obj = message.getPayload();
        log.info("transforming " + obj);
        XMLStreamReader reader = (XMLStreamReader) obj;
        TransformerFactory tf = TransformerFactory.newInstance();

        try {
            Transformer t = tf.newTransformer();
            StringWriter writer = new StringWriter();
            t.transform(new StAXSource(reader), new StreamResult(writer));
            String serviceContractDoc = writer.toString();
            log.info("Doc " + serviceContractDoc);
            String serviceContractUri = message.getProperty("NT_SERVICE_CONTRACT_URI", PropertyScope.INVOCATION);
            String serviceContractAction = message.getProperty("NT_SERVICE_CONTRACT_ACTION", PropertyScope.INVOCATION);
            String rivtaVersion = message.getProperty("NT_RIVTA_VERSION", PropertyScope.INVOCATION);

            message.setPayload(new NtPayload(serviceContractUri, serviceContractAction, serviceContractDoc, rivtaVersion));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return message;
    }

}

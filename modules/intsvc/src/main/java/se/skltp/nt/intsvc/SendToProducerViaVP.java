package se.skltp.nt.intsvc;

import javax.xml.soap.*;
import javax.xml.stream.XMLStreamReader;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendToProducerViaVP implements Callable {

    private static final Logger log = LoggerFactory.getLogger(SendToProducerViaVP.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {

        MuleMessage message = eventContext.getMessage();
        String rivtaVersion = message.getOutboundProperty("NT_RIVTA_VERSION");
        String serviceContractUri = message.getOutboundProperty("NT_SERVICE_CONTRACT_URI");

        log.info("rivTa " + rivtaVersion + ", scUri " + serviceContractUri);

        Object obj = message.getPayload();
        XMLStreamReader reader = (XMLStreamReader) obj;

        log.info(" got " + obj);

        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage msg = mf.createMessage();

        return message;
    }
}

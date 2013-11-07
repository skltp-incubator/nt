package se.skltp.nt.mysample;

import java.util.StringTokenizer;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySampleResponseTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(MySampleResponseTransformer.class);

    /**
     * Message aware transformer that ...
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

        // Perform any message aware processing here, otherwise delegate as much as possible to pojoTransform() for easier unit testing

        return pojoTransform(message.getPayload(), outputEncoding);
    }
    
    /**
     * Simple pojo transformer method that can be tested with plain unit testing...
     */
    public Object pojoTransform(Object src, String outputEncoding) throws TransformerException {
        log.debug("Transforming payload: {}", src);


		return src;

	}

	private String createFault(String errorMessage) {
		return 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
		"<soap:Fault xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
		"<faultcode>soap:Server</faultcode>" + 
		"<faultstring>" + errorMessage + "</faultstring>" + 
		"</soap:Fault>";
	}

}
package se.skltp.nt.receiveservice;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.soitoolkit.refapps.sd.sample.schema.v1.Sample;

public class ReceiveServiceRequestTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(ReceiveServiceRequestTransformer.class);

	private static final JaxbUtil jaxbUtil = new JaxbUtil(Sample.class);

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
	protected Object pojoTransform(Object src, String encoding) throws TransformerException {


		log.debug("Transforming payload: {}", src);
		
		return src;

	}
}
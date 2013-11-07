package se.skltp.nt.myoneway;

import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.MessageFactory;
import org.mule.transformer.AbstractTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyOneWayTransformer extends AbstractTransformer {

	private static final Logger log = LoggerFactory.getLogger(MyOneWayTransformer.class);
	
	/**
	 * Simplest possible transformer that ...
	 */
	@Override
	protected Object doTransform(Object src, String encoding) throws TransformerException {

		Object outMsg = null;
		
		// Simplest possible sample transformation, convert leading "A" to "1" and "B" to "2", throw an exception for all other input
		if (src instanceof String) {
			String inMsg = (String)src;

			if (inMsg.startsWith("A")) {
				log.debug("A message of type A is transformed to a message of type 1");
				outMsg = "1" + inMsg.substring(1);

			} else if (inMsg.startsWith("B")) {
				log.debug("A message of type B is transformed to a message of type 2");
				outMsg = "2" + inMsg.substring(1);
				
			} else {
				throw new TransformerException(MessageFactory.createStaticMessage("Unknown content: " + inMsg), this);
			}

		} else {
			throw new TransformerException(MessageFactory.createStaticMessage("Unknown source: " + src.getClass().getName()), this);
		}
		
		return outMsg;
	}
}
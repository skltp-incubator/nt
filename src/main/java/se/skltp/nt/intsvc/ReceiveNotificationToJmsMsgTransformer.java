package se.skltp.nt.intsvc;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ObjectFactory;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationType;


public class ReceiveNotificationToJmsMsgTransformer extends AbstractMessageTransformer {

		private static JaxbUtil jabxUtil = new JaxbUtil(ReceiveNotificationType.class);
		private static ObjectFactory of = new ObjectFactory();

		@Override
		public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {
		
			Object[] objArr = (Object[])message.getPayload();
			String logicalAddress = (String)objArr[0];
			ReceiveNotificationType request = (ReceiveNotificationType)objArr[1];
			String jmsMsg = jabxUtil.marshal(of.createReceiveNotification(request));
			
			message.setPayload(jmsMsg);
			message.setOutboundProperty("logicalAddress", logicalAddress);

			return message;
		}

	}

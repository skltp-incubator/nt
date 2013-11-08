package se.skltp.nt.intsvc;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.riv.infrastructure.itintegration.notification.ReceiveNotification.v1.ReceiveNotificationResponderInterface;
import se.riv.infrastructure.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationResponseType;
import se.riv.infrastructure.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationType;
import se.riv.infrastructure.itintegration.notification.ReceiveNotificationResponder.v1.ResultCodeEnum;


@WebService(serviceName = "ReceiveNotificationResponderService",
        endpointInterface = "se.riv.infrastructure.itintegration.notification.ReceiveNotification.v1.ReceiveNotificationResponderInterface",
        portName = "ReceiveNotificationResponderPort",
        targetNamespace = "urn:riv:infrastructure:itintegration:notification:ReceiveNotification:1:rivtabp21",
        wsdlLocation = "schemas/interactions/ReceiveNotificationInteraction/ReceiveNotificationInteraction_1.0_RIVTABP21.wsdl")
public class ReceiveNotificationImpl implements ReceiveNotificationResponderInterface {
	
	private static final Logger log = LoggerFactory.getLogger(ReceiveNotificationImpl.class);
	
	@Override
	public ReceiveNotificationResponseType receiveNotification(
			String logicalAddress,
			ReceiveNotificationType parameters)
	{
		
		log.error("##### - receiveNotification");
		System.err.println("##### - serr receiveNotification");
		ReceiveNotificationResponseType response = new ReceiveNotificationResponseType();
		
		response.setResultCode(ResultCodeEnum.OK);

		return response;
	}

}

package se.skltp.nt.intsvc;

import javax.jws.WebService;

import riv.infrastructure.itintegration.notification.receivenotificationresponder._1.ReceiveNotificationResponseType;
import riv.infrastructure.itintegration.notification.receivenotificationresponder._1.ReceiveNotificationType;
import se.riv.infrastructure.itintegration.notification.ReceiveNotification.v1.ReceiveNotificationResponderInterface;

@WebService(serviceName = "ReceiveNotificationResponderService",
        endpointInterface = "se.riv.infrastructure.itintegration.notification.ReceiveNotification.v1.ReceiveNotificationResponderInterface",
        portName = "ReceiveNotificationResponderPort",
        targetNamespace = "urn:riv:infrastructure:itintegration:notification:ReceiveNotification:1:rivtabp21",
        wsdlLocation = "schemas/interactions/ReceiveNotificationInteraction/ReceiveNotificationInteraction_1.0_RIVTABP21.wsdl")
public class ReceiveNotificationImpl implements ReceiveNotificationResponderInterface {

	@Override
	public ReceiveNotificationResponseType receiveNotification(
			String logicalAddress,
			ReceiveNotificationType parameters) {
		// TODO Auto-generated method stub
		return null;
	}

}

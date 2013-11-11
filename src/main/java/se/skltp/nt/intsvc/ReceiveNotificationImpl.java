package se.skltp.nt.intsvc;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.riv.itintegration.notification.ReceiveNotification.v1.ReceiveNotificationResponderInterface;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationResponseType;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationType;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ResultCodeEnum;

@WebService(serviceName = "ReceiveNotificationResponderService",
        endpointInterface = "se.riv.itintegration.notification.ReceiveNotification.v1.ReceiveNotificationResponderInterface",
        portName = "ReceiveNotificationResponderPort",
        targetNamespace = "urn:riv:itintegration:notification:ReceiveNotification:1:rivtabp21",
        wsdlLocation = "schemas/interactions/ReceiveNotificationInteraction/ReceiveNotificationInteraction_1.0_RIVTABP21.wsdl")
public class ReceiveNotificationImpl implements ReceiveNotificationResponderInterface {
	
	private static final Logger log = LoggerFactory.getLogger(ReceiveNotificationImpl.class);


    @Override
    public ReceiveNotificationResponseType receiveNotification(@WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true) String logicalAddress, @WebParam(partName = "parameters", name = "ReceiveNotification", targetNamespace = "urn:riv:itintegration:notification:ReceiveNotificationResponder:1") ReceiveNotificationType parameters) {
        ReceiveNotificationResponseType response = new ReceiveNotificationResponseType();

        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }
}

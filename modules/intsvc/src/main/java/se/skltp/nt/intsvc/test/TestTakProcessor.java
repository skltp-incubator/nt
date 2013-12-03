package se.skltp.nt.intsvc.test;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rivta.itintegration.test.TestProcessTakNotification.v1.rivtabp21.TestProcessTakNotificationResponderInterface;
import se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21.TestProcessTakNotificationResponseType;
import se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21.TestProcessTakNotificationType;

/**
 * Acts as a producer for testing in VP.
 * <p/>
 * Should be removed from final product.
 *
 * @author mats.olsson@callistaenterprise.se
 */
@WebService(
        serviceName = "UpdateResponderService",
        portName = "UpdateResponderPort",
        targetNamespace = "urn:riv:itintegration:engagementindex:Update:1:rivtabp21")
public class TestTakProcessor implements TestProcessTakNotificationResponderInterface {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(TestTakProcessor.class);


    @Override
    public TestProcessTakNotificationResponseType testProcessTakNotification(
            @WebParam(
                    partName = "LogicalAddress",
                    name = "LogicalAddress",
                    targetNamespace = "urn:riv:itintegration:registry:1",
                    header = true)
            String logicalAddress,
            @WebParam(
                    partName = "parameters",
                    name = "TestProcessTakNotification",
                    targetNamespace = "urn:riv:itintegration:test:TestProcessTakNotificationResponder:1")
            TestProcessTakNotificationType parameters
    ) {

        LOG.info("ProcTakNot@" + logicalAddress + ", serviceContract " + parameters.getRouteChange().getServiceContract().getServiceContractNamespace());
        return new TestProcessTakNotificationResponseType();
    }

}

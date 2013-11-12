package se.skltp.nt.receiveservice;

import static org.junit.Assert.*;
 
import static se.skltp.nt.NtMuleServer.getAddress;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
 
 
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;


import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationResponseType;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ResultCodeEnum;

 
public class ReceiveServiceIntegrationTest extends AbstractTestCase {
 
	
	private static final Logger log = LoggerFactory.getLogger(ReceiveServiceIntegrationTest.class);
	
 
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
 

	private static final String DEFAULT_SERVICE_ADDRESS = getAddress("RECEIVE-SERVICE_INBOUND_URL");
 
 
	private static final String ERROR_LOG_QUEUE = "SOITOOLKIT.LOG.ERROR";
	private AbstractJmsTestUtil jmsUtil = null;
 

    public ReceiveServiceIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

	protected String getConfigResources() {
		return "soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
  
		"nt-common.xml," +
        "receive-service.xml"; /*+
		"teststub-services/init-dynamic-flows.xml," +
	    "teststub-services/receive-notification-teststub-service.xml";*/
    }

    @Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		doSetUpJms();
  
     }

	private void doSetUpJms() {
		// TODO: Fix lazy init of JMS connection et al so that we can create jmsutil in the declaration
		// (The embedded ActiveMQ queue manager is not yet started by Mule when jmsutil is delcared...)
		if (jmsUtil == null) jmsUtil = new ActiveMqJmsTestUtil();
		
 
		// Clear queues used for error handling
		jmsUtil.clearQueues(ERROR_LOG_QUEUE);
    }


    @Test
    public void test_ok() {
    	ReceiveServiceTestConsumer consumer = new ReceiveServiceTestConsumer(DEFAULT_SERVICE_ADDRESS);
		ReceiveNotificationResponseType response = consumer.callService("Foo-1", "subj-1", "cat-1");
		assertEquals(ResultCodeEnum.OK,  response.getResultCode());
	}
 

}

package se.skltp.nt.myoneway;

import static org.junit.Assert.*;

import static org.soitoolkit.commons.mule.mime.MimeUtil.sendFileAsMultipartHttpPost;
import static se.skltp.nt.NtMuleServer.getAddress;
 

import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import javax.sql.DataSource;

import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transformer.TransformerMessagingException;
import org.mule.transport.email.MailProperties;
import org.mule.transport.file.FileConnector;
import org.mule.transport.ftp.FtpConnector;
import org.mule.transport.sftp.SftpConnector;

import org.soitoolkit.commons.mule.jdbc.JdbcScriptEngine;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
 
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
 
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.Dispatcher;
import org.soitoolkit.commons.mule.util.MiscUtil;
import org.soitoolkit.commons.mule.util.MuleUtil;
import org.soitoolkit.commons.mule.ftp.FtpUtil;
import org.soitoolkit.commons.mule.sftp.SftpUtil;
import org.soitoolkit.commons.mule.file.FileUtil;
import org.soitoolkit.commons.mule.mail.MailUtil;
import org.soitoolkit.commons.mule.jdbc.JdbcUtil;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
public class MyOneWayIntegrationTest extends AbstractTestCase {
 
	
	private static final Logger log = LoggerFactory.getLogger(MyOneWayIntegrationTest.class);
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("nt-config");

 


	private static final String OUT_VM_QUEUE = rb.getString("MYONEWAY_OUT_VM_QUEUE");
 

 
 
 
 
 
	private static final String ERROR_LOG_QUEUE = "SOITOOLKIT.LOG.ERROR";
	private AbstractJmsTestUtil jmsUtil = null;

 
 
 
 
	
    /**
     *
     * DLQ tests expects the following setup in activemq.xml (in the <policyEntry> - element):
     *                   <deadLetterStrategy>
     *                     <!--
     *                      Use the prefix 'DLQ.' for the destination name, and make
     *                      the DLQ a queue rather than a topic
     *                     -->
     *                     <individualDeadLetterStrategy queuePrefix="DLQ." useQueueForQueueMessages="true" />
     *                   </deadLetterStrategy>
     * 
     */
    public MyOneWayIntegrationTest() {
      
 
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

	protected String getConfigResources() {
        return "soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
  
  
  


		"nt-common.xml," +
		"myOneWay-service.xml," +
		"teststub-services/myOneWay-teststub-service.xml";
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
    public void testMyOneWay_ok() throws JMSException {

		Map<String, String> props = new HashMap<String, String>();
    	final  String inputFile   = "src/test/resources/testfiles/myOneWay/input.txt";
    	String expectedResultFile = "src/test/resources/testfiles/myOneWay/expected-result.txt";
        String receivingService   = "myOneWay-teststub-service";

		final  int timeout        = 5000;
 
		String input          = MiscUtil.readFileAsString(inputFile);
		String expectedResult = MiscUtil.readFileAsString(expectedResultFile);


		final String url       = getAddress("MYONEWAY_INBOUND_URL");
		Dispatcher dispatcher = new Dispatcher() {
			public void doDispatch() {
				sendFileAsMultipartHttpPost(url, new File(inputFile), "payload", false, timeout);
			}
		};




		// Invoke the service and wait for the transformed message to arrive at the receiving teststub service
		MuleMessage reply = dispatchAndWaitForServiceComponent(dispatcher, receivingService, timeout);

 


		String transformedMessage = reply.getPayload().toString();
		
		// Remove any new lines added by various transports and transformers, e.g. the email-related ones...
		transformedMessage = MiscUtil.removeTrailingNewLines(transformedMessage);

		// Verify the result, i.e. the transformed message
        assertEquals(expectedResult, transformedMessage);

 

 

		// Verify error-queue
        assertEquals(0, jmsUtil.browseMessagesOnQueue(ERROR_LOG_QUEUE).size());
    }

    
}

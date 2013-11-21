/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.nt.intsvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.Dispatcher;
import se.riv.itintegration.engagementindex.ProcessNotificationResponder.v1.ProcessNotificationResponseType;
import se.riv.itintegration.engagementindex.ProcessNotificationResponder.v1.ProcessNotificationType;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationResponseType;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationType;
import se.skltp.nt.NtMuleServer;
import se.skltp.nt.intsvc.ReceiveNotificationTestProducer.ReceiveData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class EndToEndIntegrationTest extends AbstractTestCase implements MessageListener {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(EndToEndIntegrationTest.class);

    private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

    private static final String LOGICAL_ADDRESS = rb.getString("NT_HSA_ID");

    @SuppressWarnings("unused")
    private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
    private static final String NOTIFICATION_SERVICE_ADDRESS = NtMuleServer.getAddress("RECEIVE-SERVICE_INBOUND_URL");

    private TextMessage receiveNotificationMessage = null;

    @Override
    public void onMessage(Message message) {
        receiveNotificationMessage = (TextMessage) message;
    }

    public EndToEndIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
        return
                "soitoolkit-mule-jms-connector-activemq-embedded.xml," +
                        "nt-common.xml," +
                        "receive-service.xml," +
                        "teststub-services/init-dynamic-flows.xml," +
                        "teststub-services/receive-notification-teststub-service.xml," +
                        "teststub-services/process-notification-teststub-service.xml";
    }

    @Before
    public void setUp() throws Exception {

        // Clear queues used for the tests
        getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE);

    }


    /**
     * Verify that a Publisher can send a ReceiveNotification through the system to the Subscribers.
     */
    @Test
    public void endToEnd_OK() {

        String subject = "TheSubject";
        String category = "TheCategory";

        final String logicalAddress = LOGICAL_ADDRESS;

        final ReceiveNotificationType recRequest = createReceiveNotificationRequest(subject, category);
        MuleMessage recNotMessage = dispatchAndWaitForServiceComponent(new Dispatcher() {
            public void doDispatch() {
                ReceiveNotificationResponseType response = new ReceiveNotificationTestConsumer(NOTIFICATION_SERVICE_ADDRESS).callService(logicalAddress, recRequest);
            }
        }, "receive-notification-teststub-service", EI_TEST_TIMEOUT);

        final ProcessNotificationType procRequest = createProcessNotificationRequest();
        MuleMessage procNotMessage = dispatchAndWaitForServiceComponent(new Dispatcher() {
            public void doDispatch() {
                ProcessNotificationResponseType response = new ProcessNotificationTestConsumer(NOTIFICATION_SERVICE_ADDRESS).callService(logicalAddress, procRequest);
            }
        }, "process-notification-teststub-service", EI_TEST_TIMEOUT);

        ReceiveNotificationResponseType rnrt = (ReceiveNotificationResponseType) recNotMessage.getPayload();
        assertEquals(se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ResultCodeEnum.OK, rnrt.getResultCode());

        ProcessNotificationResponseType pnrt = (ProcessNotificationResponseType) procNotMessage.getPayload();
        assertEquals(se.riv.itintegration.engagementindex.v1.ResultCodeEnum.OK, pnrt.getResultCode());

        // Wait a short while for all background processing to complete
        waitForBackgroundProcessing();

        // Expect no error logs
        assertQueueDepth(ERROR_LOG_QUEUE, 0);

        // Expect  info entries?
        assertQueueDepth(INFO_LOG_QUEUE, 10);

        List<ReceiveNotificationTestProducer.ReceiveData> recNotMessages = ReceiveNotificationTestProducer.getReceiveDataList();

        Set<String> recAddresses = new HashSet<String>();
        assertEquals(2, recNotMessages.size());
        for ( ReceiveData data : recNotMessages ) {
            assertEquals(subject, data.parameters.getSubject());
            assertEquals(category, data.parameters.getCategory());
            recAddresses.add(data.logicalAddress);
        }
        assertEquals(2, recAddresses.size());
        // should find FOO-x from config...
        assertTrue(recAddresses.contains("FOO-1"));
        assertTrue(recAddresses.contains("FOO-2"));

        List<ProcessNotificationTestProducer.ReceiveData> procNotMessages = ProcessNotificationTestProducer.getReceiveDataList();

        Set<String> addresses = new HashSet<String>();
        assertEquals(2, procNotMessages.size());
        for ( ProcessNotificationTestProducer.ReceiveData data : procNotMessages ) {
            addresses.add(data.logicalAddress);
        }
        assertEquals(2, addresses.size());
        // should find FOO-x from config...
        assertTrue(addresses.contains("FOO-1"));
        assertTrue(addresses.contains("FOO-2"));


    }


}
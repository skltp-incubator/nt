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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.Dispatcher;
import se.rivta.itintegration.engagementindex.ProcessNotificationResponder.v1.rivtabp21.ProcessNotificationType;
import se.rivta.itintegration.notification.ReceiveNotificationResponder.v1.rivtabp21.ReceiveNotificationType;
import se.skltp.nt.NtMuleServer;
import se.skltp.nt.intsvc.ReceiveNotificationTestProducer.ReceiveData;

import static org.junit.Assert.assertEquals;


public class EndToEndIntegrationTest extends AbstractTestCase implements MessageListener {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(EndToEndIntegrationTest.class);

    private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

    private static final String LOGICAL_ADDRESS = rb.getString("NT_HSA_ID");

    @SuppressWarnings("unused")
    private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
    private static final String RECEIVE_ADDRESS = NtMuleServer.getAddress("NOTIFY_TEST_OUTBOUND_URL");
    private static final String PROCESS_ADDRESS = NtMuleServer.getAddress("PROCESS_TEST_OUTBOUND_URL");

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

        final String logicalAddress = LOGICAL_ADDRESS;

        // during testing, we can't rely on a VP to re-route to producers
        CreatePropsAndConvertToStringTransformer.setEndpointPortOverride("8083");

        final ProcessNotificationType procRequest = createProcessNotificationRequest();
        dispatchAndWaitForServiceComponent(new Dispatcher() {
            public void doDispatch() {
                new ProcessNotificationTestConsumer(PROCESS_ADDRESS).callService(logicalAddress, procRequest);
            }
        }, "process-notification-teststub-service", EI_TEST_TIMEOUT);


        final ReceiveNotificationType[] requests = {
                createReceiveNotificationRequest("domain-1", "category-1"),
                createReceiveNotificationRequest("domain-1", "category-2"),
                createReceiveNotificationRequest("domain-1", ""),
                createReceiveNotificationRequest("domain-2", "category-1"),
                createReceiveNotificationRequest("domain-2", "category-2"),
                createReceiveNotificationRequest("domain-2", ""),
                createReceiveNotificationRequest("", ""),
        };

        for ( final ReceiveNotificationType request : requests ) {
            dispatchAndWaitForServiceComponent(new Dispatcher() {
                public void doDispatch() {
                    new ReceiveNotificationTestConsumer(RECEIVE_ADDRESS).callService(logicalAddress, request);
                }
            }, "receive-notification-teststub-service", EI_TEST_TIMEOUT);
        }

        // Wait a short while for all background processing to complete
        waitForBackgroundProcessing();

        // Expect no error logs
        assertQueueDepth(ERROR_LOG_QUEUE, 0);

        // Expect  info entries?
        assertQueueDepth(INFO_LOG_QUEUE, 48);

        List<ReceiveNotificationTestProducer.ReceiveData> recNotMessages = ReceiveNotificationTestProducer.getReceiveDataList();

        Map<String, Integer> recMsgCount = new HashMap<String, Integer>();
        assertEquals(14, recNotMessages.size());
        for ( ReceiveData data : recNotMessages ) {
            Integer count = recMsgCount.get(data.logicalAddress);
            count = (count == null ? 1 : count + 1);
            recMsgCount.put(data.logicalAddress, count);
        }
        assertEquals(3, recMsgCount.size());
        assertEquals(3, (int) recMsgCount.get("Foo-1")); // gets only domain-1, cat-1 -> 3
        assertEquals(4, (int) recMsgCount.get("Foo-2")); // gets only domain-1, any cat -> 4
        assertEquals(7, (int) recMsgCount.get("Foo-3")); // gets every message -> 7

        List<ProcessNotificationTestProducer.ReceiveData> procNotMessages = ProcessNotificationTestProducer.getReceiveDataList();

        Map<String, Integer> procMsgCount = new HashMap<String, Integer>();
        assertEquals(3, procNotMessages.size());
        for ( ProcessNotificationTestProducer.ReceiveData data : procNotMessages ) {
            Integer count = procMsgCount.get(data.logicalAddress);
            count = (count == null ? 1 : count + 1);
            procMsgCount.put(data.logicalAddress, count);
        }
        assertEquals(3, procMsgCount.size());
        assertEquals(1, (int) procMsgCount.get("Foo-1"));
        assertEquals(1, (int) procMsgCount.get("Foo-2"));
        assertEquals(1, (int) procMsgCount.get("Foo-3"));

    }


}
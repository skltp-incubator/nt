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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.mule.api.MuleMessage;
import org.mule.api.context.notification.EndpointMessageNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.mule.context.notification.EndpointMessageNotification;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.Dispatcher;
import org.soitoolkit.commons.mule.test.DispatcherMuleClientImpl;
import org.soitoolkit.commons.mule.util.MuleUtil;
import org.soitoolkit.commons.mule.util.ValueHolder;
import se.rivta.itintegration.engagementindex.ProcessNotificationResponder.v1.rivtabp21.ProcessNotificationType;
import se.rivta.itintegration.notification.ReceiveNotificationResponder.v1.rivtabp21.ReceiveNotificationType;
import se.rivta.itintegration.notification.v1.Filter;
import se.skltp.nt.NtMuleServer;
import se.skltp.nt.svc.ConfigProperties;

import static org.junit.Assert.*;

/**
 * Extends the base class in Mule, org.mule.tck.junit4.FuntionalTestCase.
 *
 * @author Magnus Larsson
 */
public abstract class AbstractTestCase extends org.soitoolkit.commons.mule.test.junit4.AbstractTestCase {

    protected static final int EI_TEST_TIMEOUT = 5000;
    protected static final int EI_SHORT_WAITTIME = 500;

    protected static final ConfigProperties rb = NtMuleServer.getConfigProperties();
    protected static final String INFO_LOG_QUEUE = rb.get("SOITOOLKIT_LOG_INFO_QUEUE");
    protected static final String ERROR_LOG_QUEUE = rb.get("SOITOOLKIT_LOG_ERROR_QUEUE");
    protected static final String NOTIFY_TOPIC = rb.get("NOTIFY_TOPIC");

    private AbstractJmsTestUtil jmsUtil = null;

    public AbstractTestCase() {
        super();
    }

    protected AbstractJmsTestUtil getJmsUtil() {

        // TODO: Fix lazy init update_of JMS connection et al so that we can create jmsutil in the declaration
        // (The embedded ActiveMQ queue manager is not yet started by Mule when jmsutil is declared...)
        if ( jmsUtil == null ) {
            String clientId = UUID.randomUUID().toString();
            jmsUtil = new ActiveMqJmsTestUtil("vm://localhost", clientId);
        }

        return jmsUtil;
    }

    protected List<Message> assertQueueDepth(String queueName, int expectedDepth) {
        List<Message> messages = getJmsUtil().browseMessagesOnQueue(queueName);
        assertEquals(expectedDepth, messages.size());
        return messages;
    }

    protected List<Message> assertQueueContainsMessage(String queueName, String expectedText) {
        try {
            List<Message> messages = getJmsUtil().browseMessagesOnQueue(queueName);

            System.err.println("MSG CNT: " + messages.size());
            for ( Message message : messages ) {
                String text = ((TextMessage) message).getText();
                System.err.println("MSG: " + text);
                if ( text.contains(expectedText) ) return messages;
            }

            fail("Faild to find any message on the queue " + queueName + " that contains the text: " + expectedText);
            return messages;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<Message> assertQueueMatchesMessages(String queueName, String... expectedRegexps) {
        try {
            List<Message> messages = getJmsUtil().browseMessagesOnQueue(queueName);
            System.err.println("MSG CNT: " + messages.size());

            // Go through the messages in the queue checking each expected regexp, one by one
            for ( int i = 0; i < expectedRegexps.length; i++ ) {

                // The regexp to check this time
                String expectedRegexp = expectedRegexps[i];

                // Look for a match
                boolean found = false;
                for ( Message message : messages ) {
                    String text = ((TextMessage) message).getText();
                    System.err.println("MSG: " + text);
                    if ( text.matches(expectedRegexp) ) found = true;
                }
                if ( !found ) {
                    // If not found then fail with a proper mesage
                    fail("Faild to find any message on the queue " + queueName + " that contains the text: " + expectedRegexp);
                }
            }

            return messages;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }


    protected void assertRequest(String expectedXml, MuleMessage actual) {
        TextMessage actualJms = (TextMessage) actual.getPayload();
        assertRequest(expectedXml, actualJms);
    }

    protected void assertRequest(String expectedXml, TextMessage actualJms) {
        try {
            String actualXml = actualJms.getText();

            assertXml(expectedXml, actualXml);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }


    private void assertXml(String expected, String actual) {

        // We're using Xml Unit to compare results
        // Ignore whitespace and comments

        try {
            XMLUnit.setIgnoreWhitespace(true);
            XMLUnit.setIgnoreComments(true);

            // Check if XSL transformation went OK
            Diff diff = new Diff(expected, actual);

            assertTrue("XML compare failed " + diff, diff.similar());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected ReceiveNotificationType createReceiveNotificationRequest(String serviceDomain, String category) {

        ReceiveNotificationType request = new ReceiveNotificationType();

        Filter filter = new Filter();
        filter.setServiceDomain(serviceDomain);
        filter.setCategorization(category);
        request.setFilter(filter);

        return request;
    }

    protected ProcessNotificationType createProcessNotificationRequest() {

        return new ProcessNotificationType();
    }

    protected void waitForBackgroundProcessing() {
        try {
            Thread.sleep(EI_SHORT_WAITTIME);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code> and with the action <code>action</code>.
     * <p/>
     * Sample usage: TBS
     *
     * @param outboundEndpointName
     * @param action
     * @param timeout              in ms
     * @return the MuleMessage sent to the named service component
     */
    protected MuleMessage waitForDelivery(final String outboundEndpointName, final int action, long timeout) {
        return dispatchAndWaitForDelivery(null, outboundEndpointName, action, timeout);
    }

    /**
     * Sends the <code>payload</code> and <code>headers</code> to the <code>inboundEndpointAddress</code> and waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code>.
     * <p/>
     * Sample usage:
     * <tt>
     * public void testTransferKorttransaktioner() throws Exception {
     * String expectedPayload = "Yada, yada, yada...";
     * <p/>
     * MuleMessage message = dispatchAndWaitForDelivery(
     * "sftp://dfcx0346@vfin8003.volvofinans.net/sftp/vfkonto/ut",
     * expectedPayload,
     * createFileHeader("from_vfkonto.dat"),
     * "volvokort-test-endpoint",
     * TIMEOUT);
     * <p/>
     * String actualPayload = message.getPayloadAsString();
     * assertEquals(expectedPayload, actualPayload);
     * }
     * </tt>
     *
     * @param inboundEndpointAddress
     * @param payload
     * @param headers
     * @param outboundEndpointName
     * @param action                 as specified by org.mule.context.notification.EndpointMessageNotification: MESSAGE_RECEIVED, MESSAGE_DISPATCHED, MESSAGE_SENT or MESSAGE_REQUESTED
     * @param timeout                in ms
     * @return the received MuleMEssage on the outboundEndpoint
     */
    protected MuleMessage dispatchAndWaitForDelivery(String inboundEndpointAddress, Object payload, Map<String, String> headers, final String outboundEndpointName, final int action, long timeout) {
        return dispatchAndWaitForDelivery(new DispatcherMuleClientImpl(muleContext, inboundEndpointAddress, payload, headers), outboundEndpointName, action, timeout);
    }


    /**
     * Use the Dispatcher to send a asynchronous message and waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code> and with the action <code>action</code>.
     *
     * @param dispatcher
     * @param outboundEndpointName
     * @param action               as specified by org.mule.context.notification.EndpointMessageNotification: MESSAGE_RECEIVED, MESSAGE_DISPATCHED, MESSAGE_SENT or MESSAGE_REQUESTED
     * @param timeout              in ms
     * @return the received MuleMEssage on the outboundEndpoint
     */
    @SuppressWarnings("rawtypes")
    protected MuleMessage dispatchAndWaitForDelivery(Dispatcher dispatcher, final String outboundEndpointName, final int action, long timeout) {

        // Declare MuleMessage to return
        final ValueHolder<MuleMessage> receivedMessageHolder = new ValueHolder<MuleMessage>();

        // Declare countdown latch and listener
        final CountDownLatch latch = new CountDownLatch(1);
        EndpointMessageNotificationListener listener = null;

        try {

            // Next create a listener that listens for dispatch events on the outbound endpoint
            listener = new EndpointMessageNotificationListener() {
                public void onNotification(ServerNotification notification) {
                    if ( logger.isDebugEnabled() )
                        logger.debug("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName() + ")");

                    // Only care about EndpointMessageNotification
                    if ( notification instanceof EndpointMessageNotification ) {
                        EndpointMessageNotification endpointNotification = (EndpointMessageNotification) notification;

                        // Extract action and name update_of the endpoint
                        int actualAction = endpointNotification.getAction();
                        String actualEndpoint = MuleUtil.getEndpointName(endpointNotification);

                        // If it is a dispatch event on our outbound endpoint then countdown the latch.
                        if ( logger.isDebugEnabled() ) {
                            logger.debug(actualAction == action);
                            logger.debug(actualEndpoint.equals(outboundEndpointName));
                        }
                        if ( actualAction == action && actualEndpoint.equals(outboundEndpointName) ) {
                            if ( logger.isDebugEnabled() )
                                logger.debug("Expected notification received on " + actualEndpoint + " (action: " + endpointNotification.getActionName() + "), time to countdown the latch");
                            receivedMessageHolder.value = (MuleMessage) endpointNotification.getSource();
                            latch.countDown();

                        } else {
                            if ( logger.isDebugEnabled() )
                                logger.debug("A not matching notification received on " + actualEndpoint + " (action: " + endpointNotification.getActionName() + "), continue to wait for the right one...");
                        }
                    }
                }
            };

            // Now register the listener
            muleContext.getNotificationManager().addListener(listener);

            // Perform the actual dispatch, if any...
            if ( dispatcher != null ) {
                dispatcher.doDispatch();
            }

            // Wait for the delivery to occur...
            if ( logger.isDebugEnabled() ) logger.debug("Waiting for message to be delivered to the endpoint...");
            boolean workDone = latch.await(timeout, TimeUnit.MILLISECONDS);
            if ( logger.isDebugEnabled() )
                logger.debug((workDone) ? "Message delivered, continue..." : "No message delivered, timeout occurred!");

            // Raise a fault if the test timed out
            assertTrue("Test timed out. It took more than " + timeout + " milliseconds. If this error occurs the test probably needs a longer time out (on your computer/network)", workDone);

        } catch (Exception e) {
            e.printStackTrace();
            fail("An unexpected error occurred: " + e.getMessage());

        } finally {
            // Always remove the listener if created
            if ( listener != null ) muleContext.getNotificationManager().removeListener(listener);
        }

        return receivedMessageHolder.value;
    }

}
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rivta.itintegration.notification.ReceiveNotification.v1.rivtabp21.ReceiveNotificationResponderInterface;
import se.rivta.itintegration.notification.ReceiveNotificationResponder.v1.rivtabp21.ReceiveNotificationResponseType;
import se.rivta.itintegration.notification.ReceiveNotificationResponder.v1.rivtabp21.ReceiveNotificationType;
import se.skltp.nt.NtMuleServer;

public class ReceiveNotificationTestConsumer extends AbstractTestConsumer<ReceiveNotificationResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(ReceiveNotificationTestConsumer.class);

	public static void main(String[] args) {
		String serviceAddress = NtMuleServer.getAddress("RECEIVE_NOTIFICATION_WEB_SERVICE_URL");
		
		ReceiveNotificationTestConsumer consumer = new ReceiveNotificationTestConsumer(serviceAddress);

		ReceiveNotificationType request = new ReceiveNotificationType();
		ReceiveNotificationResponseType response = consumer.callService("logical-adress", request);
	}

	public ReceiveNotificationTestConsumer(String serviceAddress) {
	    
		// Setup a web service proxy for communication using HTTPS with Mutual Authentication
		super(ReceiveNotificationResponderInterface.class, serviceAddress);

		if (serviceAddress == null) {
			throw new RuntimeException("Null address");
		}

	}

	public ReceiveNotificationResponseType callService(String logicalAddress, ReceiveNotificationType request) {

		log.debug("Calling ReceiveNotification-soap-service ");

		ReceiveNotificationResponseType response = _service.receiveNotification(logicalAddress, request);
        return response;
	}
}

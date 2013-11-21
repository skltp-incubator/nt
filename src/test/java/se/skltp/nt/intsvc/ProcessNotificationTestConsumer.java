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
import se.riv.itintegration.engagementindex.ProcessNotification.v1.ProcessNotificationResponderInterface;
import se.riv.itintegration.engagementindex.ProcessNotificationResponder.v1.ProcessNotificationResponseType;
import se.riv.itintegration.engagementindex.ProcessNotificationResponder.v1.ProcessNotificationType;
import se.skltp.nt.NtMuleServer;

public class ProcessNotificationTestConsumer extends AbstractTestConsumer<ProcessNotificationResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(ProcessNotificationTestConsumer.class);

	public static void main(String[] args) {
		String serviceAddress = NtMuleServer.getAddress("PROCESS_NOTIFICATION_WEB_SERVICE_URL");

		ProcessNotificationTestConsumer consumer = new ProcessNotificationTestConsumer(serviceAddress);

		ProcessNotificationType request = new ProcessNotificationType();
		ProcessNotificationResponseType response = consumer.callService("logical-adress", request);

		log.info("Returned status = " + response.getResultCode());
	}

	public ProcessNotificationTestConsumer(String serviceAddress) {
	    
		// Setup a web service proxy for communication using HTTPS with Mutual Authentication
		super(ProcessNotificationResponderInterface.class, serviceAddress);

		if (serviceAddress == null) {
			throw new RuntimeException("Null address");
		}

	}

	public ProcessNotificationResponseType callService(String logicalAddress, ProcessNotificationType request) {

		log.debug("Calling ProcessNotification-soap-service ");

		ProcessNotificationResponseType response = _service.processNotification(logicalAddress, request);
        return response;
	}
}

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

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import se.riv.itintegration.notification.ReceiveNotification.v1.ReceiveNotificationResponderInterface;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationResponseType;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationType;
import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ResultCodeEnum;

@WebService(
        serviceName = "ReceiveNotificationService", 
        portName = "ProcessNotificationPort", 
        targetNamespace = "urn:riv:itintegration:notification:ProcessNotification:1:rivtabp21")
public class ReceiveNotificationTestProducer implements ReceiveNotificationResponderInterface {

    public static final String TEST_SUBJECT_TRIGGER_TIMEOUT = "timeout";
    
	private static final Logger log = LoggerFactory.getLogger(ReceiveNotificationTestProducer.class);
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("nt-config");
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

    private void forceTimeout() {
        try {
            log.info("TestProducer forced timeout ...");
            Thread.sleep(SERVICE_TIMOUT_MS + 1000);
        } catch (InterruptedException e) {}
    }

	@Override
	@WebResult(name = "ReceiveNotificationResponse", targetNamespace = "urn:riv:itintegration:notification:ReceiveNotificationResponder:1", partName = "parameters")
	@WebMethod(operationName = "ReceiveNotification", action = "urn:riv:itintegration:notification:ReceiveNotificationResponder:1:ReceiveNotification")
	public ReceiveNotificationResponseType receiveNotification(
			@WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true) String logicalAddress,
			@WebParam(partName = "parameters", name = "ReceiveNotification", targetNamespace = "urn:riv:itintegration:notification:ReceiveNotificationResponder:1") ReceiveNotificationType parameters) {

		log.info("ReceiveNotificationTestProducer " + logicalAddress + " received a notification for subject " + parameters.getSubject() + ", cat " + parameters.getCategory());
		
        // Force a timeout based on subject
		if (TEST_SUBJECT_TRIGGER_TIMEOUT.equals(parameters.getSubject())) forceTimeout();
		addReceiveData(logicalAddress, parameters);

        ReceiveNotificationResponseType response = new ReceiveNotificationResponseType();
        response.setResultCode(ResultCodeEnum.OK);
		return response;
	}

	public static void addReceiveData(String logicalAddress, ReceiveNotificationType parameters) {
		synchronized(receiveDataList) {
			receiveDataList.add(new ReceiveData(logicalAddress, parameters));
		}
	}
	
	public static List<ReceiveData> getReceiveDataList() {
		synchronized(receiveDataList) {
			return new ArrayList<ReceiveData>(receiveDataList);
		}
	}
	
	public static class ReceiveData {
		public ReceiveData(String logicalAddress,
				ReceiveNotificationType parameters) {
			this.logicalAddress = logicalAddress;
			this.parameters = parameters;
		}
		String logicalAddress;
		ReceiveNotificationType parameters;
	}
	
	static final List<ReceiveData> receiveDataList = new ArrayList<ReceiveData>();
	
}
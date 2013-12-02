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
import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.rivta.itintegration.engagementindex.ProcessNotification.v1.rivtabp21.ProcessNotificationResponderInterface;
import se.rivta.itintegration.engagementindex.ProcessNotificationResponder.v1.rivtabp21.ProcessNotificationResponseType;
import se.rivta.itintegration.engagementindex.ProcessNotificationResponder.v1.rivtabp21.ProcessNotificationType;
import se.rivta.itintegration.engagementindex.v1.ResultCodeEnum;
import se.skltp.nt.NtMuleServer;


@WebService(
        serviceName = "ProcessNotificationService",
        portName = "ProcessNotificationPort",
        targetNamespace = "urn:riv:itintegration:engagementindex:ProcessNotification:1:rivtabp21")
public class ProcessNotificationTestProducer implements ProcessNotificationResponderInterface {

    public static final String TEST_SUBJECT_TRIGGER_TIMEOUT = "timeout";

    private static final Logger log = LoggerFactory.getLogger(ProcessNotificationTestProducer.class);
    private static final RecursiveResourceBundle rb = NtMuleServer.getRb();
    private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

    private void forceTimeout() {
        try {
            log.info("TestProducer forced timeout ...");
            Thread.sleep(SERVICE_TIMOUT_MS + 1000);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public ProcessNotificationResponseType processNotification(
            @WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true) String logicalAddress,
            @WebParam(partName = "parameters", name = "ProcessNotification", targetNamespace = "urn:riv:itintegration:engagementindex:ProcessNotificationResponder:1") ProcessNotificationType parameters) {
        log.info("ProcessNotificationTestProducer " + logicalAddress + " processNotification");
        ProcessNotificationResponseType response = new ProcessNotificationResponseType();
        response.setResultCode(ResultCodeEnum.OK);
        addReceiveData(logicalAddress, parameters);
        return response;
    }


    public static void addReceiveData(String logicalAddress, ProcessNotificationType parameters) {
        synchronized (receiveDataList) {
            receiveDataList.add(new ReceiveData(logicalAddress, parameters));
        }
    }

    public static List<ReceiveData> getReceiveDataList() {
        synchronized (receiveDataList) {
            return new ArrayList<ReceiveData>(receiveDataList);
        }
    }


    public static class ReceiveData {
        public ReceiveData(String logicalAddress,
                           ProcessNotificationType parameters) {
            this.logicalAddress = logicalAddress;
            this.parameters = parameters;
        }

        String logicalAddress;
        ProcessNotificationType parameters;
    }

    private static final List<ReceiveData> receiveDataList = new ArrayList<ReceiveData>();

}
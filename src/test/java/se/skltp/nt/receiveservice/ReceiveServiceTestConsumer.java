package se.skltp.nt.receiveservice;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.rivta.itintegration.notification.ReceiveNotification.v1.rivtabp21.ReceiveNotificationResponderInterface;
import se.rivta.itintegration.notification.ReceiveNotificationResponder.v1.rivtabp21.ReceiveNotificationResponseType;
import se.rivta.itintegration.notification.ReceiveNotificationResponder.v1.rivtabp21.ReceiveNotificationType;
import se.rivta.itintegration.notification.v1.Filter;
import se.skltp.nt.NtMuleServer;

import static se.skltp.nt.NtMuleServer.getAddress;

public class ReceiveServiceTestConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReceiveServiceTestConsumer.class);

    private static final RecursiveResourceBundle rb = NtMuleServer.getRb();

    private ReceiveNotificationResponderInterface _service = null;

    public ReceiveServiceTestConsumer(String serviceAddress) {
        JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
        proxyFactory.setServiceClass(ReceiveNotificationResponderInterface.class);
        proxyFactory.setAddress(serviceAddress);

        /*
          //Used for HTTPS
          SpringBusFactory bf = new SpringBusFactory();
          URL cxfConfig = ReceiveServiceTestConsumer.class.getClassLoader().getResource("cxf-test-consumer-config.xml");
          if (cxfConfig != null) {
              proxyFactory.setBus(bf.createBus(cxfConfig));
          }
          */

        _service = (ReceiveNotificationResponderInterface) proxyFactory.create();
    }

    public static void main(String[] args) throws Exception {
        String serviceAddress = getAddress("NT-SERVICES_INBOUND_URL");

        ReceiveServiceTestConsumer consumer = new ReceiveServiceTestConsumer(serviceAddress);
        ReceiveNotificationResponseType response = consumer.callService("Foo-1", "subj-1", "cat-1");
    }

    public ReceiveNotificationResponseType callService(String logicalAddress, String domain, String category) {
        log.debug("Calling receive-notification-soap-service with addr=" + logicalAddress + ", subj=" + domain + ", cat=" + category);
        ReceiveNotificationType request = new ReceiveNotificationType();
        Filter filter = new Filter();
        filter.setServiceDomain(domain);
        filter.setCategorization(category);
        request.setFilter(filter);
        return _service.receiveNotification(logicalAddress, request);
    }

}
package se.skltp.nt.mysample;

import static se.skltp.nt.NtMuleServer.getAddress;

import java.net.URL;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import org.soitoolkit.refapps.sd.sample.schema.v1.Sample;
import org.soitoolkit.refapps.sd.sample.schema.v1.SampleResponse;
import org.soitoolkit.refapps.sd.sample.wsdl.v1.Fault;
import org.soitoolkit.refapps.sd.sample.wsdl.v1.SampleInterface;

public class MySampleTestConsumer {

	private static final Logger log = LoggerFactory.getLogger(MySampleTestConsumer.class);

	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("nt-config");

	private SampleInterface _service = null;
	    
    public MySampleTestConsumer(String serviceAddress) {
		JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
		proxyFactory.setServiceClass(SampleInterface.class);
		proxyFactory.setAddress(serviceAddress);
		
		//Used for HTTPS
		SpringBusFactory bf = new SpringBusFactory();
		URL cxfConfig = MySampleTestConsumer.class.getClassLoader().getResource("cxf-test-consumer-config.xml");
		if (cxfConfig != null) {
			proxyFactory.setBus(bf.createBus(cxfConfig));
		}
		
		_service  = (SampleInterface) proxyFactory.create(); 
    }

    public static void main(String[] args) throws Fault {
            String serviceAddress = getAddress("MYSAMPLE_INBOUND_URL");
            String personnummer = "1234567890";

            MySampleTestConsumer consumer = new MySampleTestConsumer(serviceAddress);
            SampleResponse response = consumer.callService(personnummer);
            log.info("Returned value = " + response.getValue());
    }

    public SampleResponse callService(String id) throws Fault {
            log.debug("Calling sample-soap-service with id = {}", id);
            Sample request = new Sample();
            request.setId(id);
            return _service.sample(request);
    }	
	
}
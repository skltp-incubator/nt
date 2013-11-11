package se.skltp.nt.intsvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import se.riv.itintegration.notification.ReceiveNotificationResponder.v1.ReceiveNotificationType;


public class ChangeAddressBean {

	private static final Logger LOG = LoggerFactory.getLogger(ChangeAddressBean.class);
	    
    private String logicalAddress = null;
    
    private static JaxbUtil jabxUtil = new JaxbUtil(ReceiveNotificationType.class);

    public void setLogicalAddress(String logicalAddress) {
    	LOG.info("logical address set to {}", logicalAddress);
    	this.logicalAddress = logicalAddress;
    }

    /**
     *
     * @param requestStr
     * @return
     */
    public Object[] changeAddress(String requestStr) {

		ReceiveNotificationType notification = (ReceiveNotificationType) jabxUtil.unmarshal(requestStr);
    	
    	LOG.info("Received the request: {}", notification);
    	
	    return new Object[] { logicalAddress, notification};
    }
}
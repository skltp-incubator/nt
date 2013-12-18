package se.skltp.nt.intsvc;

import java.util.Map;

import org.mule.el.context.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter the message.
 * <p/>
 * If the message should not be processed, then we set the payload to the empty string.
 * <p/>
 * We filter by asking the TAK (service address catalogue) for
 */
public class MessageFilter {

    private static final Logger log = LoggerFactory.getLogger(MessageFilter.class);

    private SubscriberDatabase subscriberDatabase;

    public void setSubscriberDatabase(SubscriberDatabase subscriberDatabase) {
        this.subscriberDatabase = subscriberDatabase;
    }

    public boolean allows(String logicalAddress, MessageContext message) {
        Map<String, Object> inProps = message.getInboundProperties();
        String serviceContractUri = getRequired(inProps, "NT_SERVICE_CONTRACT_URI");
        String serviceDomain = getRequired(inProps, "NT_SERVICE_DOMAIN");
        String categorization = getRequired(inProps, "NT_CATEGORIZATION");
        return subscriberDatabase.subscribesTo(logicalAddress, serviceContractUri, serviceDomain, categorization);
    }

    private String getRequired(Map<String, Object> inProps, String property) {
        String result = (String) inProps.get(property);
        if ( result == null ) {
            throw new IllegalStateException("Missing property " + property + "!");
        }
        return result;
    }

}

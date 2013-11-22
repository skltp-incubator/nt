package se.skltp.nt.intsvc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the TAK-information for routing.
 * <p/>
 * Turn on DEBUG logging for information.
 *
 * @author mats.olsson@callistaenterprise.se
 */
public class SubscriberDatabase {

    private static final Logger log = LoggerFactory.getLogger(SubscriberDatabase.class);

    public static final SubscriberDatabase INSTANCE = new SubscriberDatabase();

    public static SubscriberDatabase getInstance() {
        return INSTANCE;
    }

    private HashMap<String, Map<String, Subscriber>> subscriberMap = new HashMap<String, Map<String, Subscriber>>();
    private Set<String> logicalAddressSet = new HashSet<String>();

    private SubscriberDatabase() {
    }

    /**
     * Return true if the given subscriber (identified by logical address) subscribes to the message identified
     * by the (serviceContractUri, serviceDomain, categorization) flow.
     * <p/>
     * The filtering is done by steps; ie the subscriber must subscribe to the serviceContractUri, then we
     * check the serviceDomain, then we check the categorization (per serviceDomain).
     * <p/>
     * ANY is indicated by having an empty serviceDomain and/or categorization.
     *
     * @param subscribersLogicalAddress the subscriber, identified by his logical address
     * @param serviceContractUri        identifies the service contract
     * @param serviceDomain             identifies first filter parameter (name is from TAK)
     * @param categorization            identifiest second filter parameter (name is from TAK)
     * @return true if the subscriber subscribes to the message
     */
    public boolean subscribesTo(String subscribersLogicalAddress, String serviceContractUri, String serviceDomain, String categorization) {
        if ( log.isDebugEnabled() )
            log.debug("subscribesTo('" + subscribersLogicalAddress + "', '" + serviceContractUri + "', '" + serviceDomain + "', '" + categorization + "')");
        Map<String, Subscriber> subscribers = subscriberMap.get(serviceContractUri);
        if ( subscribers == null ) {
            if ( log.isDebugEnabled() )
                log.debug("1: reject " + subscribersLogicalAddress + ", no subscribers to " + serviceContractUri);
            return false; // noone subscribes to the service contract
        }
        Subscriber subscriber = subscribers.get(subscribersLogicalAddress);
        if ( subscriber == null ) {
            if ( log.isDebugEnabled() )
                log.debug("2: reject " + subscribersLogicalAddress + ", does not produce " + serviceContractUri);
            return false; // the subscriber with the given logicalAddress does not subscribe to that serviceContract
        }
        // if the domainMap is empty, we subscribe to all domains
        return subscriber.allowed(serviceDomain, categorization);
    }

    public Set<String> getAllSubscriberLogicalAddresses() {
        return new HashSet<String>(logicalAddressSet);

    }

    private class Subscriber {
        private String logicalAddress;
        private Map<String, ServiceDomain> serviceDomains = new HashMap<String, ServiceDomain>();

        public Subscriber(String logicalAddress, String tkUri, String serviceDomain, String category) {
            this.logicalAddress = logicalAddress;
            logicalAddressSet.add(logicalAddress);
            Map<String, Subscriber> subscribers = subscriberMap.get(tkUri);
            if ( subscribers == null ) {
                subscribers = new HashMap<String, Subscriber>();
                subscriberMap.put(tkUri, subscribers);
            }
            // if a subscriber for this logical address already exists, we merge them and let this one die
            Subscriber subscriber = subscribers.get(logicalAddress);
            if ( subscriber == null ) {
                subscriber = this;
                subscribers.put(logicalAddress, subscriber);
            }
            subscriber.add(serviceDomain, category);
        }

        private void add(String serviceDomain, String category) {
            if ( serviceDomain != null ) {
                ServiceDomain domain = serviceDomains.get(serviceDomain);
                if ( domain == null ) {
                    domain = new ServiceDomain(logicalAddress, serviceDomain);
                    serviceDomains.put(serviceDomain, domain);
                }
                domain.add(category);
            }
        }


        public boolean allowed(String serviceDomain, String categorization) {
            if ( serviceDomain == null || serviceDomain.length() == 0 ) {
                // no serviceDomain given, accept everything
                if ( log.isDebugEnabled() ) log.debug("3: allows " + logicalAddress + ", no serviceDomain in message");
                return true;
            }
            if ( serviceDomains.isEmpty() ) {
                // no filtering on service domains, we accept everything
                if ( log.isDebugEnabled() ) log.debug("4: allows " + logicalAddress + ", ANY serviceDomain");
                return true;
            }
            ServiceDomain domain = serviceDomains.get(serviceDomain);
            //noinspection SimplifiableIfStatement
            if ( domain == null ) {
                // filtering active, no matching domain found, reject
                if ( log.isDebugEnabled() )
                    log.debug("5: reject " + logicalAddress + ", " + serviceDomain + " not found");
                return false;
            }
            return domain.allow(categorization);
        }
    }

    private static class ServiceDomain {

        private String logicalAddress;
        private String serviceDomain;
        private Set<String> categorizations = new HashSet<String>();

        public ServiceDomain(String logicalAddress, String serviceDomain) {
            this.logicalAddress = logicalAddress;
            this.serviceDomain = serviceDomain;
        }

        public boolean allow(String categorization) {
            if ( categorization == null || categorization.length() == 0 ) {
                // no categorization given, accept
                if ( log.isDebugEnabled() )
                    log.debug("6: allows " + logicalAddress + "/" + serviceDomain + ", no categorization in message");
                return true;
            }
            //noinspection SimplifiableIfStatement
            if ( categorizations.isEmpty() ) {
                // no filtering on categorizations active, accept all
                if ( log.isDebugEnabled() )
                    log.debug("7: allows " + logicalAddress + "/" + serviceDomain + ", ANY categorization");
                return true;
            }
            boolean result = categorizations.contains(categorization);
            if ( log.isDebugEnabled() ) {
                log.debug((result ? "8: allows " : "8: reject ") + logicalAddress + "/" + serviceDomain + "/" + categorization);
            }
            return result;
        }

        public void add(String category) {
            if ( category != null ) {
                categorizations.add(category);
            }
        }
    }


    private static final String RECEIVE_NOTIFICATION_URI = "urn:riv:itintegration:notification:ReceiveNotificationResponder:1";
    private static final String PROCESS_NOTIFICATION_URI = "urn:riv:itintegration:engagementindex:ProcessNotificationResponder:1";

    {
        // Foo-1 is specialized to domain and category
        new Subscriber("Foo-1", RECEIVE_NOTIFICATION_URI, "domain-1", "category-1");
        new Subscriber("Foo-1", PROCESS_NOTIFICATION_URI, "domain-1", "category-1");
        // Foo-2 is specialized to domain only
        new Subscriber("Foo-2", RECEIVE_NOTIFICATION_URI, "domain-1", null);
        new Subscriber("Foo-2", PROCESS_NOTIFICATION_URI, "domain-1", null);
        // Foo-3 ignores filtering completly
        new Subscriber("Foo-3", PROCESS_NOTIFICATION_URI, null, null);
        new Subscriber("Foo-3", RECEIVE_NOTIFICATION_URI, null, null);
    }


}

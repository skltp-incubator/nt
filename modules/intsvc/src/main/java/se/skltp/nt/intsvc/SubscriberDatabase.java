package se.skltp.nt.intsvc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract.v2.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract.v2.rivtabp21.GetLogicalAddresseesByServiceContractResponderService;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.FilterType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractResponseType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.LogicalAddresseeRecordType;
import se.rivta.infrastructure.itintegration.registry.getsupportedservicecontracts.v2.rivtabp21.GetSupportedServiceContractsResponderInterface;
import se.rivta.infrastructure.itintegration.registry.getsupportedservicecontracts.v2.rivtabp21.GetSupportedServiceContractsResponderService;
import se.rivta.infrastructure.itintegration.registry.getsupportedservicecontractsresponder.v2.GetSupportedServiceContractsResponseType;
import se.rivta.infrastructure.itintegration.registry.getsupportedservicecontractsresponder.v2.GetSupportedServiceContractsType;
import se.rivta.infrastructure.itintegration.registry.v2.ServiceContractNamespaceType;

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
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("nt-config");

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
        if ( log.isDebugEnabled() ) {
            log.debug("subscribesTo('" + subscribersLogicalAddress + "', '" + serviceContractUri + "', '" + serviceDomain + "', '" + categorization + "')");
        }
        Map<String, Subscriber> subscribers = subscriberMap.get(serviceContractUri);
        if ( subscribers == null ) {
            // we have not attempted to subscribe to this service contract before
            // load subscribers
            loadSubscribersFor(serviceContractUri);
            subscribers = subscriberMap.get(serviceContractUri);
            if ( subscribers == null ) {
                throw new RuntimeException("Failed to subscribe to " + serviceContractUri);
            }
        }
        Subscriber subscriber = subscribers.get(subscribersLogicalAddress);
        if ( subscriber == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("2: reject " + subscribersLogicalAddress + ", does not produce " + serviceContractUri);
            }
            return false; // the subscriber with the given logicalAddress does not subscribe to that serviceContract
        }
        // if the domainMap is empty, we subscribe to all domains
        return subscriber.allowed(serviceDomain, categorization);
    }

    /**
     * Load new subscribers for the given service contract URI.
     * <p/>
     * After this call, there will be a map of subscribers for the given service contract.
     *
     * @param serviceContractUri to load subscribers for
     */
    private void loadSubscribersFor(String serviceContractUri) {
        String ourOwnLogicalAddress = rb.getString("NT_LOGICAL_ADDRESS");

        subscriberMap.put(serviceContractUri, new HashMap<String, Subscriber>());
        Set<String> originalLogicalAddresses = new HashSet<String>(logicalAddressSet);

        // note the weird naming in the service contract - multiples are named in singular
        // the problem is the xsd-notation and the xml; it looks good there but shitty when
        // you generate the java classes. Oh well. Reassigning to correctly named variables ASAP.

        GetLogicalAddresseesByServiceContractResponseType response = getLogicalAddresses(serviceContractUri);
        List<LogicalAddresseeRecordType> addressRecords = response.getLogicalAddressRecord();
        for ( LogicalAddresseeRecordType addressRecord : addressRecords ) {
            String logicalAddress = addressRecord.getLogicalAddress();
            if (ourOwnLogicalAddress.equals(logicalAddress)) {
                continue; // avoid talking to ourselves...
            }
            List<FilterType> filters = addressRecord.getFilter();
            if ( filters.isEmpty() ) {
                // subscribe to all service domains (no filter active)
                new Subscriber(logicalAddress, serviceContractUri, null, null);
            }
            for ( FilterType filter : filters ) {
                List<String> categories = filter.getCategorization();
                if ( categories.isEmpty() ) {
                    // no category filtering
                    new Subscriber(logicalAddress, serviceContractUri, filter.getServiceDomain(), null);
                } else {
                    for ( String category : categories ) {
                        new Subscriber(logicalAddress, serviceContractUri, filter.getServiceDomain(), category);
                    }
                }
            }
        }

        // check if we have new subscribers
        Set<String> newLogicalAddresses = new HashSet<String>(logicalAddressSet);

        newLogicalAddresses.removeAll(originalLogicalAddresses);
        for ( String newLogicalAddress : newLogicalAddresses ) {
            if (log.isInfoEnabled()) {
                log.info("Adding new subscriber queue " + newLogicalAddress + " for service contract " + serviceContractUri);
            }
            createNewSubscriberFlow(newLogicalAddress);
        }
    }

    /**
     * Create a new subscriber flow.
     *
     * @param newLogicalAddress
     */
    private void createNewSubscriberFlow(String newLogicalAddress) {
        // we must be called before we start
    }

    protected GetLogicalAddresseesByServiceContractResponseType getLogicalAddresses(String serviceContractUri) {
        String vpLogicalAddress = rb.getString("VP_LOGICAL_ADDRESS");
        String getLogicalAddressWsdlUrl = rb.getString("GET_LOGICAL_ADDRESSEES_WSDL_URL");
        String ntLogicalAddress = rb.getString("NT_LOGICAL_ADDRESS");

        GetLogicalAddresseesByServiceContractType params = new GetLogicalAddresseesByServiceContractType();
        params.setServiceConsumerHsaId(ntLogicalAddress);
        ServiceContractNamespaceType ns = new ServiceContractNamespaceType();
        ns.setServiceContractNamespace(serviceContractUri);
        params.setServiceContractNameSpace(ns);
        try {
            URL url = new URL(getLogicalAddressWsdlUrl);
            GetLogicalAddresseesByServiceContractResponderInterface port = new GetLogicalAddresseesByServiceContractResponderService(url).getGetLogicalAddresseesByServiceContractResponderPort();
            Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
            Map<String, Object> msgContext = (Map<String, Object>) requestContext.get(MessageContext.HTTP_REQUEST_HEADERS);
            if (msgContext == null) {
                msgContext = new HashMap<String, Object>();
                requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, msgContext);
            }
            msgContext.put("x-rivta-original-serviceconsumer-hsaid", Collections.singletonList(vpLogicalAddress));
            return port.getLogicalAddresseesByServiceContract(vpLogicalAddress, params);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected GetSupportedServiceContractsResponseType getSupportedContracts(String logicalAddress) {
        String vpLogicalAddress = rb.getString("VP_LOGICAL_ADDRESS");
        String ntLogicalAddress = rb.getString("NT_LOGICAL_ADDRESS");
        String getSupportedContractsWsdlUrl = rb.getString("GET_SUPPORTED_CONTACTS_WSDL_URL");

        GetSupportedServiceContractsType params = new GetSupportedServiceContractsType();
        params.setLogicalAdress(vpLogicalAddress);
        params.setServiceConsumerHsaId(ntLogicalAddress);
        try {
            URL url = new URL(getSupportedContractsWsdlUrl);
            GetSupportedServiceContractsResponderInterface port = new GetSupportedServiceContractsResponderService(url).getGetSupportedServiceContractsResponderPort();
            Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
            Map<String, Object> msgContext = (Map<String, Object>) requestContext.get(MessageContext.HTTP_REQUEST_HEADERS);
            if (msgContext == null) {
                msgContext = new HashMap<String, Object>();
                requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, msgContext);
            }
            msgContext.put("x-rivta-original-serviceconsumer-hsaid", Collections.singletonList(vpLogicalAddress));
            return port.getSupportedServiceContracts(vpLogicalAddress, params);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
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
                if ( log.isDebugEnabled() ) {
                    log.debug("3: allows " + logicalAddress + ", no serviceDomain in message");
                }
                return true;
            }
            if ( serviceDomains.isEmpty() ) {
                // no filtering on service domains, we accept everything
                if ( log.isDebugEnabled() ) {
                    log.debug("4: allows " + logicalAddress + ", ANY serviceDomain");
                }
                return true;
            }
            ServiceDomain domain = serviceDomains.get(serviceDomain);
            //noinspection SimplifiableIfStatement
            if ( domain == null ) {
                // filtering active, no matching domain found, reject
                if ( log.isDebugEnabled() ) {
                    log.debug("5: reject " + logicalAddress + ", " + serviceDomain + " not found");
                }
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
                if ( log.isDebugEnabled() ) {
                    log.debug("6: allows " + logicalAddress + "/" + serviceDomain + ", no categorization in message");
                }
                return true;
            }
            //noinspection SimplifiableIfStatement
            if ( categorizations.isEmpty() ) {
                // no filtering on categorizations active, accept all
                if ( log.isDebugEnabled() ) {
                    log.debug("7: allows " + logicalAddress + "/" + serviceDomain + ", ANY categorization");
                }
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

    public static void main(String[] args) {
        SubscriberDatabase db = SubscriberDatabase.getInstance();
        String ntLogicalAddress = SubscriberDatabase.rb.getString("NT_LOGICAL_ADDRESS");
        GetSupportedServiceContractsResponseType supportedContracts = db.getSupportedContracts(ntLogicalAddress);
        List<ServiceContractNamespaceType> contracts = supportedContracts.getServiceContractNamespace();
        for ( ServiceContractNamespaceType contract : contracts ) {
            String serviceContractNs = contract.getServiceContractNamespace();
            db.loadSubscribersFor(serviceContractNs);
        }
    }


}

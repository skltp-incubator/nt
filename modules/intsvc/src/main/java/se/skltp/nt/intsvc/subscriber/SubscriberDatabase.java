package se.skltp.nt.intsvc.subscriber;

import java.util.Set;

/**
 * @author mats.olsson@callistaenterprise.se
 */
public interface SubscriberDatabase {
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
    boolean subscribesTo(String subscribersLogicalAddress, String serviceContractUri, String serviceDomain, String categorization);

    /**
     * Return all subscribers logical addresses currently in the database.
     *
     * @return the set of all subscribers logical addresses
     */
    Set<String> getAllSubscriberLogicalAddresses();

    /**
     * (Re)load the subscriber database.
     *
     * After a reload, the {@link #getAllSubscriberLogicalAddresses()} will return a list of all
     * logical addresses that may be called.
     */
    void reload();
}

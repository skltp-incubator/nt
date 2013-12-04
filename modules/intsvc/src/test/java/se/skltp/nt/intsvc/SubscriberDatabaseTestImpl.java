package se.skltp.nt.intsvc;

import se.skltp.nt.intsvc.subscriber.impl.SubscriberDatabaseImpl;

/**
 * Initialize the database properly
 *
 * @author mats.olsson@callistaenterprise.se
 */
public class SubscriberDatabaseTestImpl extends SubscriberDatabaseImpl {

    private static final String RECEIVE_NOTIFICATION_URI = "urn:riv:itintegration:notification:ReceiveNotificationResponder:1";
    private static final String PROCESS_NOTIFICATION_URI = "urn:riv:itintegration:engagementindex:ProcessNotificationResponder:1";


    /**
     * Override to load fixed set of subscribers.
     */
    @Override
    public void reload() {
        // Foo-1 is specialized to domain and category
        new Subscriber("Foo-1", RECEIVE_NOTIFICATION_URI, "domain-1", "category-1");
        new Subscriber("Foo-1", PROCESS_NOTIFICATION_URI, "domain-1", "category-1");
        // Foo-2 is specialized to domain only
        new Subscriber("Foo-2", RECEIVE_NOTIFICATION_URI, "domain-1", null);
        new Subscriber("Foo-2", PROCESS_NOTIFICATION_URI, "domain-1", null);
        // Foo-3 ignores filtering completly
        new Subscriber("Foo-3", PROCESS_NOTIFICATION_URI, null, null);
        new Subscriber("Foo-3", RECEIVE_NOTIFICATION_URI, null, null);
        // Foo-4 subscribes only to receive notification
        new Subscriber("Foo-4", RECEIVE_NOTIFICATION_URI, null, null);

        describe();
    }
}

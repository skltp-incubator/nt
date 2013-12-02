package se.skltp.nt.intsvc;

/**
 * Verify that we can look up services via TAK
 * <p/>
 * In order for this to work, we need to configure TAK with a Notification-producer
 * that produces service contracts.
 * <p/>
 * So we need a logical address for Notification and a couple of contracts that it is supposed
 * to produce.
 * <p/>
 * To avoid confusion, we do not want Notification to produce ProcessNotificatin (as
 * that would cause us to receive ProcessNotification from any EI-testing going on).
 * <p/>
 * So lets come up with a couple of fake service contracts (TestProcessTakNotification, TestProcessEiNotification)
 * and a fake logicalAddress (or just Test?).
 * <p/>
 * Then we need to setup a service contract producer for TestProcessTakNotification and add it to the flows we
 * produce.
 *
 * @author mats.olsson@callistaenterprise.se
 */
public class SubscriberDatabaseTest {


}

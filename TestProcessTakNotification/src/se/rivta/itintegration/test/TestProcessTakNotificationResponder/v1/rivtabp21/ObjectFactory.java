
package se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _TestProcessTakNotificationResponse_QNAME = new QName("urn:riv:itintegration:test:TestProcessTakNotificationResponder:1", "TestProcessTakNotificationResponse");
    private final static QName _TestProcessTakNotification_QNAME = new QName("urn:riv:itintegration:test:TestProcessTakNotificationResponder:1", "TestProcessTakNotification");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21.TestProcessTakNotificationType }
     * 
     */
    public TestProcessTakNotificationType createTestProcessTakNotificationType() {
        return new TestProcessTakNotificationType();
    }

    /**
     * Create an instance of {@link se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21.TestProcessTakNotificationResponseType }
     * 
     */
    public TestProcessTakNotificationResponseType createTestProcessTakNotificationResponseType() {
        return new TestProcessTakNotificationResponseType();
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21.TestProcessTakNotificationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:riv:itintegration:test:TestProcessTakNotificationResponder:1", name = "TestProcessTakNotificationResponse")
    public JAXBElement<TestProcessTakNotificationResponseType> createTestProcessTakNotificationResponse(TestProcessTakNotificationResponseType value) {
        return new JAXBElement<TestProcessTakNotificationResponseType>(_TestProcessTakNotificationResponse_QNAME, TestProcessTakNotificationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21.TestProcessTakNotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:riv:itintegration:test:TestProcessTakNotificationResponder:1", name = "TestProcessTakNotification")
    public JAXBElement<TestProcessTakNotificationType> createTestProcessTakNotification(TestProcessTakNotificationType value) {
        return new JAXBElement<TestProcessTakNotificationType>(_TestProcessTakNotification_QNAME, TestProcessTakNotificationType.class, null, value);
    }

}

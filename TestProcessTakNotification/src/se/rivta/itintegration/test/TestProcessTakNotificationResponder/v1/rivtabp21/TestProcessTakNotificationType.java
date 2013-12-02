
package se.rivta.itintegration.test.TestProcessTakNotificationResponder.v1.rivtabp21;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;
import se.rivta.itintegration.notification.v1.Filter;
import se.rivta.itintegration.test.v1.RouteChangeType;


/**
 * <p>Java class for TestProcessTakNotificationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TestProcessTakNotificationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filter" type="{urn:riv:itintegration:notification:1}Filter" minOccurs="0"/>
 *         &lt;element name="routeChange" type="{urn:riv:itintegration:test:1}RouteChangeType"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestProcessTakNotificationType", propOrder = {
    "filter",
    "routeChange",
    "any"
})
public class TestProcessTakNotificationType {

    protected Filter filter;
    @XmlElement(required = true)
    protected RouteChangeType routeChange;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link se.rivta.itintegration.notification.v1.Filter }
     *     
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link se.rivta.itintegration.notification.v1.Filter }
     *     
     */
    public void setFilter(Filter value) {
        this.filter = value;
    }

    /**
     * Gets the value of the routeChange property.
     * 
     * @return
     *     possible object is
     *     {@link se.rivta.itintegration.test.v1.RouteChangeType }
     *     
     */
    public RouteChangeType getRouteChange() {
        return routeChange;
    }

    /**
     * Sets the value of the routeChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link se.rivta.itintegration.test.v1.RouteChangeType }
     *     
     */
    public void setRouteChange(RouteChangeType value) {
        this.routeChange = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link org.w3c.dom.Element }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

}

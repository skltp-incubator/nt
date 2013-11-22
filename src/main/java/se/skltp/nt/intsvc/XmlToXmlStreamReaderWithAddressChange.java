package se.skltp.nt.intsvc;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.module.xml.stax.DelegateXMLStreamReader;
import org.mule.module.xml.transformer.XmlToXMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mats.olsson@callistaenterprise.se
 */
@SuppressWarnings("UnusedDeclaration") // used in backend-flow
public class XmlToXmlStreamReaderWithAddressChange extends XmlToXMLStreamReader {
    private static final Logger log = LoggerFactory.getLogger(XmlToXmlStreamReaderWithAddressChange.class);

    private String logicalAddress;

    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {
        return new AddressChangingXmlStreamReader((XMLStreamReader) super.transformMessage(message, encoding), logicalAddress);
    }

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

    /**
     * Responsible for replacing the logical address in the soap header with the given address
     */
    private static class AddressChangingXmlStreamReader extends DelegateXMLStreamReader {

        public static final QName QNAME_TO = new QName("http://www.w3.org/2005/08/addressing", "To");
        public static final QName QNAME_LOGICAL_ADDRESS = new QName("urn:riv:itintegration:registry:1", "LogicalAddress");
        public static final QName QNAME_SOAP_HEADER = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Header");

        private String logicalAddress;
        private boolean inHeader = false;
        private boolean inLogicalAddress = false;

        public AddressChangingXmlStreamReader(XMLStreamReader reader, String logicalAddress) {
            super(reader);
            this.logicalAddress = logicalAddress;
        }

        @Override
        public int next() throws XMLStreamException {
            int result = super.next();
            if ( result == START_ELEMENT ) {

                if ( QNAME_SOAP_HEADER.equals(getName()) ) {
                    inHeader = true;
                }

                if ( inHeader && (QNAME_LOGICAL_ADDRESS.equals(getName()) || QNAME_TO.equals(getName())) ) {
                    inLogicalAddress = true;
                }
            }
            if ( result == END_ELEMENT ) {
                inLogicalAddress = false;
                if ( QNAME_SOAP_HEADER.equals(getName()) ) {
                    inHeader = false;
                }
            }
            return result;
        }

        /**
         * Check in getText() for when we are in a logicalAddress node and return our logicalAddress instead
         * of whatever is there.
         * <p/>
         * Note: assumes that getText() is responsible for returning the text. This is verified every time
         * we run any integration tests, so it isn't much of a risk.
         *
         * @return
         */
        @Override
        public String getText() {
            if ( inLogicalAddress ) {
                log.debug("Replaced logicalAddress " + super.getText() + " with " + logicalAddress);
                return logicalAddress;
            }
            return super.getText();
        }
    }
}

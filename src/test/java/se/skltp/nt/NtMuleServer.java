package se.skltp.nt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.StandaloneMuleServer;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.skltp.nt.intsvc.CreatePropsAndConvertToStringTransformer;


public class NtMuleServer {


	public static final String MULE_SERVER_ID   = "nt";
 

	private static final Logger logger = LoggerFactory.getLogger(NtMuleServer.class);
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("nt-config", "nt-test-config");

	public static void main(String[] args) throws Exception {
	
 
        // Configure the mule-server:
        //
        // Arg #1: The name of the Mule Server
        //
        // Arg #2: Start teststub-services if true
        //         Note: Actually enables the spring-beans-profile "soitoolkit-teststubs" in the file "src/main/app/nt-common.xml"
        //
        // Arg #3: Start services if true 
        //         Note: Actually loads all *-service.xml files that are specified in the file "src/main/app/mule-deploy.properties"
        //
        StandaloneMuleServer muleServer = new StandaloneMuleServer(MULE_SERVER_ID, true, true);

        // during testing, we can't rely on a VP to re-route to producers
        CreatePropsAndConvertToStringTransformer.setEndpointPortOverride("8083");

        // Start the server
		muleServer.run();
	}



    /**
     * Address based on usage of the servlet-transport and a config-property for the URI-part
     * 
     * @param serviceUrlPropertyName
     * @return
     */
    public static String getAddress(String serviceUrlPropertyName) {

        String url = rb.getString(serviceUrlPropertyName);

	    logger.info("URL: {}", url);
    	return url;
 
    }

    public static String getProperty(String name) {
        return rb.getString(name);
    }

    public static RecursiveResourceBundle getRb() {
        return rb;
    }
}
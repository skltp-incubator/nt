package se.skltp.nt;


import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.StandaloneMuleServer;
import se.skltp.nt.svc.ConfigProperties;
import se.skltp.nt.svc.impl.ConfigPropertiesImpl;


public class NtMuleServer {


	public static final String MULE_SERVER_ID   = "nt";
 

	private static final Logger logger = LoggerFactory.getLogger(NtMuleServer.class);
    private static ConfigProperties configProperties = new ConfigPropertiesImpl("nt-config", "nt-config-override");

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

        Field f = muleServer.getClass().getDeclaredField("muleConfig");
        f.setAccessible(true);
        logger.info("muleConfig: " + f.get(muleServer));
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

        String url = configProperties.get(serviceUrlPropertyName);

	    logger.info("URL: {}", url);
    	return url;
 
    }

    public static String getProperty(String name) {
        return configProperties.get(name);
    }

    public static ConfigProperties getConfigProperties() {
        return configProperties;
    }
}
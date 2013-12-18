package se.skltp.nt.svc.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import se.skltp.nt.svc.ConfigProperties;

/**
 * Configuration file implementation for use with spring.
 *
 * Just a standard map with support for loading using injection.
 *
 * @author mats.olsson@callistaenterprise.se
 */
public class ConfigPropertiesImpl implements ConfigProperties {

    private RecursiveResourceBundle resourceBundle;


    public ConfigPropertiesImpl(String ... files) {
        setConfigFiles(files);
    }

    public ConfigPropertiesImpl() {
    }

    // Used for injection
    @SuppressWarnings("UnusedDeclaration")
    public void setConfigFiles(String[] configFiles) {
        // load the files and store the resulting properties in a read-write map
        resourceBundle = new RecursiveResourceBundle(configFiles);
    }

    public String get(String key) {
        return resourceBundle.getString(key);
    }

    @Override
    public Map<String, Object> getAsPropertyMap() {
        Map<String, Object> result = new HashMap<String, Object>();
        Properties properties = resourceBundle.getProperties();
        for ( Map.Entry<Object, Object> entry : properties.entrySet() ) {
            result.put((String) entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public void put(String key, String value) {
        resourceBundle.getProperties().put(key, value);
    }

}

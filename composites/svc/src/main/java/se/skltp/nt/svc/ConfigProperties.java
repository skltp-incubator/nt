package se.skltp.nt.svc;

import java.util.Map;

/**
 * Allows access to config properties.
 *
 * @author mats.olsson@callistaenterprise.se
 */
public interface ConfigProperties {

    /**
     * Return the resolved value for the key
     *
     * @param key property to get
     * @return resolved value
     */
    public String get(String key);

    /**
     * Return a copy of the properties and any extra properties added.
     *
     * @return new map of the properties
     */
    public Map<String,Object> getAsPropertyMap();

    /**
     * Add a new (resolvable) property.
     *
     * @param key name for property
     * @param value of property
     */
    void put(String key, String value);
}

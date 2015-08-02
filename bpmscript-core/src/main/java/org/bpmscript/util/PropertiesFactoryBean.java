package org.bpmscript.util;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;

/**
 * A factory bean that returns a {@link Properties} object based on the values
 * of a {@link Map}. Useful in Spring for properties that are set by reference
 * rather than by value (as is the case with {@link Properties} configuration)
 */
public class PropertiesFactoryBean implements FactoryBean {

    private Map<String, String> map = null;

    /**
     * get the object from the bean
     */
    public Object getObject() throws Exception {
        Properties props = null;
        if (map != null) {
            props = new Properties();
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                props.put(key, value);
            }
        }
        return props;
    }

    public Class<?> getObjectType() {
        return Properties.class;
    }

    public boolean isSingleton() {
        return false;
    }

    /**
     * Set the map
     * 
     * @param map the map to set
     */
    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}

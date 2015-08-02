package org.bpmscript.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;

import org.bpmscript.BpmScriptException;
import org.bpmscript.paging.IOrderBy;
import org.springframework.beans.BeanUtils;

/**
 * Uses reflection to do sorting
 */
public class ReflectionComparator implements Comparator<Object> {

    /**
     * the list of columns to sort on
     */
    private final List<IOrderBy> orderBys;

    /**
     * @param orderBys
     */
    public ReflectionComparator(List<IOrderBy> orderBys) {
        this.orderBys = orderBys;
    }

    /**
     * Reflectively get the value of a property
     * 
     * @param object the object to query
     * @param property the property to look for
     * @return the value of the property
     * @throws BpmScriptException if something goes wrong
     */
    Object getProperty(Object object, String property) throws BpmScriptException {
        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(object.getClass(), property);
        try {
            return propertyDescriptor.getReadMethod().invoke(object, (Object[]) null);
        } catch (IllegalArgumentException e) {
            throw new BpmScriptException(e);
        } catch (IllegalAccessException e) {
            throw new BpmScriptException(e);
        } catch (InvocationTargetException e) {
            throw new BpmScriptException(e);
        }
    }

    /**
     * Compare two objects using the orderby fields to sort by
     */
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2) {
    	for (IOrderBy orderBy : orderBys) {
    		try {
    			Comparable c1 = (Comparable) getProperty(o1,
    					orderBy.getField());
    			Comparable c2 = (Comparable) getProperty(o2,
    					orderBy.getField());
    			int compareTo = c1.compareTo(c2);
    			if(compareTo != 0) {
    				return orderBy.isAsc() ? compareTo : -compareTo; 
    			}
    		} catch (BpmScriptException e) {
    			throw new RuntimeException(e);
    		}
    	}
    	return 0;
    }
}
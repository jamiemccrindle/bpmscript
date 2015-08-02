package org.bpmscript.correlation;

import java.util.List;

/**
 * ICorrelation's are passed to the ICorrelationService in the addCorrelation method.
 * 
 * The consist of a list of correlation criteria. In expression terms, the criteria can
 * be thought of as separated by "and" expressions. A string example of a correlation
 * could be:
 * 
 * message.getInvoiceId() == "2314" and message.getCustomerType() == "Prospect"
 */
public interface ICorrelation {
    
    /**
     * Get the criteria for this correlation
     * 
     * @return a list of correlation criteria. should not be null.
     */
	List<ICorrelationCriteria> getCriteria();
}

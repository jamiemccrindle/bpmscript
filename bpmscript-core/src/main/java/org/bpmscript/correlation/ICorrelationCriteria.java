package org.bpmscript.correlation;

/**
 * A pair of correlation criteria, specifically a string expression in a particular
 * expression language e.g. message.getInvoiceId() and a literal value e.g. "q2423"
 */
public interface ICorrelationCriteria {

    /**
     * @return the expression for this correlation criteria. should not be null
     */
	String getExpression();
	
	/**
	 * @return the value associated with this criteria. must be a literal.
	 */
	Object getValue();
}

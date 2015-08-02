package org.bpmscript.correlation.memory;

import java.io.Serializable;

import org.bpmscript.correlation.ICorrelationCriteria;

public class CorrelationCriteria implements ICorrelationCriteria, Serializable {

    private static final long serialVersionUID = -2558666133521277367L;
    
    private Object value;
	private String expression;
	
	public CorrelationCriteria(String expression, Object value) {
		super();
		this.expression = expression;
		this.value = value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

}

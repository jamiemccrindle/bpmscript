package org.bpmscript.correlation.memory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bpmscript.correlation.ICorrelation;
import org.bpmscript.correlation.ICorrelationCriteria;

public class Correlation implements ICorrelation, Serializable {

    private static final long serialVersionUID = 801512860204361699L;
    
    private List<ICorrelationCriteria> criteria = new ArrayList<ICorrelationCriteria>();
	
	public List<ICorrelationCriteria> getCriteria() {
		return criteria;
	}
	public void setCriteria(List<ICorrelationCriteria> criteria) {
		this.criteria = criteria;
	}
	// TODO: consider singular / plural here
	public void addCriteria(String name, Object value) {
		CorrelationCriteria criterion = new CorrelationCriteria(name, value);
		criteria.add(criterion);
	}
	
}

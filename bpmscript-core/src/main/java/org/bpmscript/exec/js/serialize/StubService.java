package org.bpmscript.exec.js.serialize;


/**
 * An implementation of the {@link IStubService} that contains functions to stubs
 * and objects to stubs.
 */
public class StubService implements IStubService {

	private Stubs functionStubs = null;
	private Stubs objectStubs = null;
	
	/**
	 * Create a new Stub Service
	 *  
	 * @param functionStubs the function stubs
	 * @param objectStubs the object stubs
	 */
	public StubService(Stubs functionStubs, Stubs objectStubs) {
		this.functionStubs = functionStubs;
		this.objectStubs = objectStubs;
	}

	/**
	 * Get either an Object or Function for a stub depending on the class
	 * of the stub.
	 */
	public Object getObject(Stub stub) {
		if(stub instanceof FunctionStub) {
			Object result = functionStubs.stubsToObject.get(stub);
			return result;
		} else {
			Object result = objectStubs.stubsToObject.get(stub);
			return result;
		}
	}

	/**
	 * Gets a stub for an object. Looks in both the objectsToStubs map as
	 * well as the functionsToStubs map. Returns null if it couldn't be found
	 * in either.
	 */
	public Stub getStub(Object object) {
		Stub result = objectStubs.objectsToStub.get(object);
		if(result != null) {
			return result;
		}
		return functionStubs.objectsToStub.get(object);
	}
	
}
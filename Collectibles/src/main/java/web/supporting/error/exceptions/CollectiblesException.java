package web.supporting.error.exceptions;

import java.util.Collection;
import java.util.List;

public abstract class CollectiblesException extends Exception{

	private String errorCode;
	private String errormessage;
	private Collection<String> parameters = null; 
	
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMessage() {
		return errormessage;
	}
	public void setErrorMessage(String errormessage) {
		this.errormessage = errormessage;
	}
	public Collection<String> getParameters() {
		return parameters;
	}
	public void setParameters(Collection<String> parameters) {
		this.parameters = parameters;
	}
	
	
}

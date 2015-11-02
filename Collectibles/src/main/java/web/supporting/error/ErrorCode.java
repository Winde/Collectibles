package web.supporting.error;

import java.util.Collection;

import web.supporting.error.exceptions.CollectiblesException;

public class ErrorCode {

	private String errorCode;
	private String text;
	private Collection<String> parameters;
	
	public ErrorCode(CollectiblesException ex){
		this.errorCode = ex.getErrorCode();
		this.text = ex.getErrorMessage();
		this.parameters = ex.getParameters();
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public Collection<String> getParameters() {
		return parameters;
	}

	public void setParameters(Collection<String> parameters) {
		this.parameters = parameters;
	}
	
}

package web.supporting.error.exceptions;

public class GenericException extends CollectiblesException {

	private String errorCode;
	private String message;
	
	public GenericException(){
		this.setErrorCode("GenericError");
		this.setErrorMessage("An error has occurred");
		this.setParameters(null);
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMessage() {
		return message;
	}
	public void setErrorMessage(String message) {
		this.message = message;
	}
	
	
	
}

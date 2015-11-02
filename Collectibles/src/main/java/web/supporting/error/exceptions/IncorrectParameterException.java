package web.supporting.error.exceptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class IncorrectParameterException extends CollectiblesException {

	public IncorrectParameterException(String[] parameters){
		this(Arrays.asList(parameters));		
	}
	
	public IncorrectParameterException(Collection<String> parameters){
		this.setErrorCode("IncorrectParameterError");
		this.setErrorMessage("The input for this action has an error");		
		this.setParameters(parameters);
	}
	
}

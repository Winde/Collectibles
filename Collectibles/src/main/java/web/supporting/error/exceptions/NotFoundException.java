package web.supporting.error.exceptions;

import java.util.Arrays;
import java.util.List;

public class NotFoundException extends CollectiblesException{


	public NotFoundException(){
		this((List<String>) null);
	}
	
	public NotFoundException(String[] parameters){
		this(Arrays.asList(parameters));
	}
	
	public NotFoundException(List<String> parameters){
		this.setErrorCode("NotFoundError");
		this.setErrorMessage("No results were found");
		this.setParameters(parameters);
	}
}

package web.controllers;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import model.dataobjects.CategoryValue;
import model.dataobjects.validator.DaoValidator;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import web.supporting.error.ErrorCode;
import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.GenericException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotFoundException;

public abstract class CollectiblesController {

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ErrorCode error(Exception ex,final HttpServletResponse response){
		ex.printStackTrace();
		ErrorCode errorCode = new ErrorCode(new GenericException());		
		response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR  );
		return errorCode;
	}
	
	@ExceptionHandler(NotFoundException.class)
	@ResponseBody
	public ErrorCode notFoundError(NotFoundException ex,final HttpServletResponse response){
		ErrorCode errorCode = new ErrorCode(ex);		
		response.setStatus( HttpServletResponse.SC_NOT_FOUND  );
		return errorCode;
	}
	
	@ExceptionHandler(CollectiblesException.class)
	@ResponseBody
	public ErrorCode collectiblesError(CollectiblesException ex,final HttpServletResponse response){
		ErrorCode errorCode = new ErrorCode(ex);		
		response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR  );
		return errorCode;
	}
	
	protected void validate(Object dataobject) throws IncorrectParameterException{
		DaoValidator validator = DaoValidator.getValidator(dataobject);
		if (validator!=null){
			Collection<String> errors = validator.validate(dataobject);
			if (errors!=null && errors.size()>0){
				throw new IncorrectParameterException(errors);
			}
		}			
	}
	
	protected Long getId(String stringId) {
		Long id = null;
		try{
			id = Long.parseLong(stringId);
		}catch(Exception ex) {}
		return id;
	}
}

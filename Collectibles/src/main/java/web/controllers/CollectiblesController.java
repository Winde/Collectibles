package web.controllers;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import model.connection.AbstractProductInfoConnector;
import model.dataobjects.CategoryValue;
import model.dataobjects.serializable.ConnectorInfo;
import model.dataobjects.serializable.SerializableProduct;
import model.dataobjects.validator.DaoValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import web.supporting.error.ErrorCode;
import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.GenericException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotEnoughTimeToParseException;
import web.supporting.error.exceptions.NotFoundException;

public abstract class CollectiblesController {

	private static final String headerForDisableHttpAuthPopup = "WWW-Authenticate";
	private static final String headerValueForDisableHttpAuthPopup = "FormBased";
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractProductInfoConnector.class);
		
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ErrorCode error(Exception ex,final HttpServletResponse response){
		logger.error("Generic unhandled Controller level exception", ex);
		ErrorCode errorCode = new ErrorCode(new GenericException());		
		response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR  );
		return errorCode;
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseBody
	public ErrorCode accessDenied(Exception ex,final HttpServletResponse response){
		logger.error("Access denied", ex);
		ErrorCode errorCode = new ErrorCode(new GenericException());		
		response.setStatus( HttpServletResponse.SC_FORBIDDEN  );
		response.setHeader(headerForDisableHttpAuthPopup, headerValueForDisableHttpAuthPopup);
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
	
	@ExceptionHandler(NotEnoughTimeToParseException.class)
	@ResponseBody
	public SerializableProduct error(NotEnoughTimeToParseException ex,final HttpServletResponse response){
		logger.error("Interrupted request without finished parsing");
		response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		return ex.getProduct();
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

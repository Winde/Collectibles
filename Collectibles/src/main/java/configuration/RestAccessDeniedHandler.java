package configuration;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler{
	
	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response, 
			AccessDeniedException accessDeniedException)  throws IOException{
		System.out.println("**********************CALLED HANDLER *****************");
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	}
	
}

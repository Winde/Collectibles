package configuration.security.jwt;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtAuthenticationOnSuccess implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationOnSuccess.class);		
	
	private TokenAuthenticationService tokenAuthenticationService = null;

	public JwtAuthenticationOnSuccess(TokenAuthenticationService tokenAuthenticationService){
		this.tokenAuthenticationService  = tokenAuthenticationService;
	}
	
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
	
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof UserAuthentication) {
			UserAuthentication userAuth = (UserAuthentication) authentication;
			UserDetailsImpl details = userAuth.getDetails();
			if (details!=null){
				logger.info("Adding token for user "+ userAuth + " to response, INTENDED AFTER AUTH!!!");
				tokenAuthenticationService.addAuthentication(httpResponse, userAuth);
			}
		} else {
			httpResponse.addHeader("X-AUTH-EXPIRE", "true");
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}



}
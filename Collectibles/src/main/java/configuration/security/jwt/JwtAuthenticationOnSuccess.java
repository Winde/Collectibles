package configuration.security.jwt;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtAuthenticationOnSuccess implements Filter {

	
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
				tokenAuthenticationService.addAuthentication(httpResponse, userAuth);
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}



}
package configuration.security.jwt;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter{

	private static final Logger logger = LoggerFactory.getLogger(StatelessLoginFilter.class);
	 	
	private UserDetailsServiceImpl userDetailsService = null;
	private TokenAuthenticationService tokenAuthenticationService = null;

	public StatelessLoginFilter(String urlMapping, TokenAuthenticationService tokenAuthenticationService,
            UserDetailsServiceImpl userDetailsService, AuthenticationManager authManager) {
        
		super(new AntPathRequestMatcher(urlMapping));
        this.userDetailsService = userDetailsService;
        this.tokenAuthenticationService = tokenAuthenticationService;
        setAuthenticationManager(authManager);
    }
	
	

	@Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

		logger.info("Obtaining Login Authentication");		
		
		String username = request.getParameter("username"); 
		String password = request.getParameter("password");
		
		final UsernamePasswordAuthenticationToken loginToken = new UsernamePasswordAuthenticationToken(username,password);
        
		logger.info("Obtained LoginToken");		
		
		return getAuthenticationManager().authenticate(loginToken);
    }
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain, Authentication authentication) throws IOException, ServletException {


		logger.info("Succesful Login Authentication");
		
		final UserDetailsImpl authenticatedUser = userDetailsService.loadUserByUsername(authentication.getName());
		final UserAuthentication userAuthentication = new UserAuthentication(authenticatedUser);

		logger.info("Adding token");
		
		tokenAuthenticationService.addAuthentication(response, userAuthentication);
		SecurityContextHolder.getContext().setAuthentication(userAuthentication);
		if (authenticatedUser!=null && authenticatedUser.getUser()!=null && authenticatedUser.getUser().getRoles()!=null){
			Collection<String> roles = authenticatedUser.getUser().getRoles();
			ObjectMapper mapper = new ObjectMapper();
			String rolesJson = mapper.writeValueAsString(roles);
			if (rolesJson!=null){
				response.getWriter().write(rolesJson);
				response.setContentType("Content-Type:application/json;charset=UTF-8");
			}
		}
		
	}	


}

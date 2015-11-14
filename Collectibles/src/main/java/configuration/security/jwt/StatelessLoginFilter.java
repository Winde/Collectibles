package configuration.security.jwt;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter{

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

		System.out.println("Obtaining Login Authentication");
		
		String username = request.getParameter("username"); 
		String password = request.getParameter("password");
		
		final UsernamePasswordAuthenticationToken loginToken = new UsernamePasswordAuthenticationToken(username,password);
        
		System.out.println("Obtained LoginToken");
		
		return getAuthenticationManager().authenticate(loginToken);
    }
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain, Authentication authentication) throws IOException, ServletException {


		System.out.println("Succesful Login Authentication");
		
		final UserDetailsImpl authenticatedUser = userDetailsService.loadUserByUsername(authentication.getName());
		final UserAuthentication userAuthentication = new UserAuthentication(authenticatedUser);

		System.out.println("Adding token");
		
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

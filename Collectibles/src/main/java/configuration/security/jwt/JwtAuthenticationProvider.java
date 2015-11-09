package configuration.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider{

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {

		/*		UserAuthentication auth = (UserAuthentication) authentication;
		String username = String.valueOf(auth.getPrincipal());
		String password = String.valueOf(auth.getCredentials());

		// 1. Use the username to load the data for the user, including authorities and password.
		UserDetailsImpl user = userDetailsService.loadUserByUsername(username);
				
		if (!user.getPassword().equals(password)) {
			throw new BadCredentialsException("Bad Credentials");
		}

		// 3. Preferably clear the password in the user object before storing in authentication object
		//user.clearPassword();

		// 4. Return an authenticated token, containing user data and authorities  
*/
		return authentication;
			
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.isAssignableFrom(UserAuthentication.class);
	}

}

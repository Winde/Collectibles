package configuration.security.jwt;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class UserAuthentication implements Authentication {

	UserDetailsImpl user = null;
	
	public UserAuthentication(UserDetailsImpl authenticatedUser) {
		user = authenticatedUser; 
	}

	@Override
	public String getName() {
		return user.getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getAuthorities(); 		
	}

	@Override
	public Object getCredentials() {
		return user.getPassword();
	}

	@Override
	public UserDetailsImpl getDetails() {
		return user;
	}

	@Override
	public Object getPrincipal() {
		return user.getUsername();
	}

	@Override
	public boolean isAuthenticated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	public String toString(){
		return "{"+ this.getName() + "}";
	}
}

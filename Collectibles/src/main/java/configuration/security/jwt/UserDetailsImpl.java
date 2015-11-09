package configuration.security.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.dataobjects.User;

public class UserDetailsImpl implements UserDetails{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private User user = null;
	
	private Long expires = null;
	
	public UserDetailsImpl(User user){
		this.user = user;
	}
	
	@Override
	public String getUsername() {
		return user.getUsername();
	}
		
	@Override
	@JsonIgnore
	public String getPassword() {
		return user.getPassword();
	}
	
	public Long getExpires() {
		return expires;
	}

	public void setExpires(Long expires) {
		this.expires = expires;
	}
	
	public static UserDetailsImpl fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		UserDetailsImpl result = null;
		try {
			result = mapper.readValue(json, UserDetailsImpl.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public static byte[] toJSON(UserDetailsImpl userDetails) {		
		byte[] result = null;
		ObjectMapper mapper = new ObjectMapper();
		
		if (userDetails!=null){		
			try {
				mapper.writeValueAsBytes(userDetails);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return result;
		
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("USER"));
		authorities.add(new SimpleGrantedAuthority("ADMIN"));
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		return authorities;
	}



	@Override
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}


}

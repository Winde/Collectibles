package configuration.security.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import model.dataobjects.User;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class UserDetailsImpl implements UserDetails{


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private User user = null;
	
	private Long expires = null;
	
	public UserDetailsImpl(){
		
	}
	
	public UserDetailsImpl(User user){
		this.user = user;
	}
	
	@Override
	@JsonIgnore
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static UserDetailsImpl fromJSON(String json){
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		SimpleModule module = new SimpleModule("SimpleGrantDeSerializer", new Version(0, 1, 0, "SNAPSHOT","winde","simplegrant"));
		module.addDeserializer(SimpleGrantedAuthority.class, new SimpleGrantedAuthorityDeserializer());		
		objectMapper.registerModule(module);
		
		UserDetailsImpl result = null;
		try {
			result = objectMapper.readValue(json, UserDetailsImpl.class);
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
				result = mapper.writeValueAsBytes(userDetails);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return result;
		
	}

	@Override
	public Collection<SimpleGrantedAuthority> getAuthorities() {
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if (user!=null){			
			Collection<String> roles = user.getRoles();
			if (roles!=null){				
				for (String role: roles) {
					SimpleGrantedAuthority auth = new SimpleGrantedAuthority(role);
					authorities.add(auth);
				}
			}
		}
		return authorities;
	}


	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return true;
	}


}

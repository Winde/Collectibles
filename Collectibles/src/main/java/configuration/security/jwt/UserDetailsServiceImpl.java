package configuration.security.jwt;

import model.dataobjects.User;
import model.persistence.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService  {

	@Autowired
	private UserRepository userRepository;
	
	public UserDetailsImpl loadUserByUsername(String name) {
		
		UserDetailsImpl userDetails = null; 
		User userData = userRepository.findOne(name);
				
		if (userData!=null){
			userDetails = new UserDetailsImpl(userData); 	
		}
		
		if (userDetails==null){
			throw new UsernameNotFoundException("User not found");
		}
		return userDetails;		
	}

}

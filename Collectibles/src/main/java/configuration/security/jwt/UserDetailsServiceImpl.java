package configuration.security.jwt;

import model.dataobjects.User;
import model.persistence.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService  {

	@Autowired
	private UserRepository userRepository;
	
	public UserDetailsImpl loadUserByUsername(String name) {
		
		UserDetailsImpl userDetails = null; 
		//User userData = userRepository.findOne(name);
		
		User userData = new User();
		userData.setPassword("password");
		userData.setUsername("username");
		
		if (userData!=null){
			userDetails = new UserDetailsImpl(userRepository.findOne(name)); 	
		}
		return userDetails;		
	}

}

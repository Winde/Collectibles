package web.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.dataobjects.User;
import model.dataobjects.serializable.SerializableUser;
import model.persistence.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;

@RestController
public class UserController extends CollectiblesController{

	@Autowired
	private UserRepository userRepository;
		
	@Secured(value = { "ROLE_USER" })
	@RequestMapping(value="/users/list/", method = RequestMethod.GET)
	public Collection<SerializableUser> listUsers() throws CollectiblesException{					
		
		Iterable<User> users = userRepository.findAll();
		List<SerializableUser> serializedUsers = new ArrayList<>();
		if (users!=null){
		
			for (User user: users){
				serializedUsers.add(new SerializableUser(user));
			}
		}
		return serializedUsers;
	}
	
}

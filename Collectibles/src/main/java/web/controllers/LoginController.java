package web.controllers;

import javax.servlet.http.HttpServletRequest;

import model.dataobjects.User;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController extends CollectiblesController {

	@Secured({"ROLE_ADMIN"})
	@RequestMapping("/login")
	public User login(HttpServletRequest request){
		//request.getSession();
		
		return new User();		
	}
	
	@Secured({"ROLE_ADMIN"})
	@RequestMapping("/checksession")
	public User checksession(HttpServletRequest request){
		//request.getSession();
		
		return new User();		
	}
	
}

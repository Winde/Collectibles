package model.persistence;

import model.dataobjects.User;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository  extends CrudRepository<User,String>{

	
}

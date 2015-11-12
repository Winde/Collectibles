package model.persistence;


import model.dataobjects.Author;

import org.springframework.data.repository.CrudRepository;

public interface AuthorRepository extends CrudRepository<Author,String>{

}

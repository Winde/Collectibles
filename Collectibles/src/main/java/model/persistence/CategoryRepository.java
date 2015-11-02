package model.persistence;

import model.dataobjects.Category;

import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category,Long>,CategoryRepositoryCustom{
	
	
}

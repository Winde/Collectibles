package model.persistence;

import model.dataobjects.Image;

import org.springframework.data.repository.CrudRepository;

public interface ImageRepository extends CrudRepository<Image,Long>{

}

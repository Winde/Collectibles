package model.persistence;

import model.dataobjects.Image;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

public interface ImageRepository extends CrudRepository<Image,Long>{

	@Cacheable(value="images", cacheManager="cacheManager")
	public Image findOne(Long id);
}

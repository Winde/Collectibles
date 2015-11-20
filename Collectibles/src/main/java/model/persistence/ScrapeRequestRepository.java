package model.persistence;

import java.util.List;

import model.dataobjects.inmemory.ScrapeRequest;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ScrapeRequestRepository extends CrudRepository<ScrapeRequest,Long>{

	public List<ScrapeRequest> findByProductIdAndConnector(Long productId, String connector);

	@Query("select s from ScrapeRequest s where connector = :connector ORDER BY liveRequest DESC, requestTime ASC")
	public List<ScrapeRequest> findOldestByConnector(@Param("connector") String connector);	
}

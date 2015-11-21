package model.persistence.queues;

import java.util.Collection;
import java.util.List;

import model.dataobjects.inmemory.ScrapeRequest;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ScrapeRequestRepository {

	//@Query("select s from ScrapeRequest s where connector = :connector ORDER BY liveRequest DESC, requestTime ASC")
	public ScrapeRequest findOldestByConnector(@Param("connector") String connector);	
	
	public boolean saveIgnoreCheck(ScrapeRequest scrapeReq);

	public boolean save(ScrapeRequest scrapeReq);

	public boolean save(Iterable<ScrapeRequest> requests);

	public Boolean checkPending(Collection<ScrapeRequest> requests);

	public Boolean checkPending(ScrapeRequest request);

	public boolean markAsCompleted(ScrapeRequest scrapeReq);


	
}

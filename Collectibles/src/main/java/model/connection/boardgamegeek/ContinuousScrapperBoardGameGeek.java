package model.connection.boardgamegeek;

import model.connection.ContinuousScrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ContinuousScrapperBoardGameGeek{

	@Autowired
	private BoardGameGeekConnector connector;
	
	@Autowired
	private ContinuousScrapper scrapper;
	
	
	@Scheduled(initialDelay=2000, fixedDelay=5000)
	public void doScrape() {
		scrapper.doScrape(connector);
    }
	
}

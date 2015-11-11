package model.connection.goodreads;

import java.io.FileNotFoundException;
import java.util.List;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class GoodReadsItemLookupService extends ProductInfoLookupServiceXML {

	private String key = null;
	
	@Autowired
	public GoodReadsItemLookupService(@Value("${goodreads.public.key}") String key) {
		this.key = key;
	}
	
	public String getLookupUrl(String id){
		String url = "https://www.goodreads.com/book/isbn?isbn="+id+"&key="+key;
		System.out.println("Goodreads url for fetch data: " + url);
		return url;
	}
			
	public String getImageUrl(Document doc){		
		return this.getField(doc, "/GoodreadsResponse/book/image_url");		
	}
	
	public byte [] getImageData(Document doc) throws TooFastConnectionException{
		byte [] data = null;
		String url = this.getImageUrl(doc);
		if (url!=null && url.indexOf("/nophoto/")<0){
			data = this.fetchImage(url);
		}
		return data;
	}
	
	public String getDescription(Document doc){
		return this.getField(doc, "/GoodreadsResponse/book/description");		
	}
	
	public Integer getPublicationYear(Document doc){
		Integer result = null;
		try{
			result = Integer.parseInt(this.getField(doc, "/GoodreadsResponse/book/work/original_publication_year"));
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return result;
	}
	
	public List<String> getAuthors(Document doc){
		return this.getFields(doc, "/GoodreadsResponse/book/authors/author/name");		
	}
	
	public String getSeriesUrl(Document doc){
		String url = null;
		String baseUrl = "https://www.goodreads.com/series/";
		String series = this.getField(doc, "/GoodreadsResponse/book/series_works/series_work/series/id");
		if (series !=null) {
			url = baseUrl + series;
		}
		return url;		
	}

	@Override
	public Document fetchDocFromId(String id) throws TooFastConnectionException {
		String requestUrl = this.getLookupUrl(id);
		Document doc = null;
		try{
			doc = this.fetchDocFromUrl(requestUrl);
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
			return null;
		}
		return doc;
	}

	@Override
	public String getAmazonUrl(Document doc) throws TooFastConnectionException {
		return null;
	}
	
	
}

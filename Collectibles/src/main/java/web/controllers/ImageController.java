package web.controllers;

import java.util.ArrayList;
import java.util.List;

import model.dataobjects.Author;
import model.dataobjects.Image;
import model.dataobjects.Image.ImageSimpleView;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotFoundException;

import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class ImageController extends CollectiblesController{

	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private AuthorRepository authorRepository;
	
	
	@RequestMapping(value="/image/{id}", method = RequestMethod.GET)
	public Image getImage(@PathVariable String id) throws CollectiblesException{
		Long imageId = this.getId(id);
		if (imageId==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			Image image = imageRepository.findOne(imageId);
			if (image==null){
				throw new NotFoundException();
			} else {
				return image;
			}
		}
	}
	
	@JsonView(ImageSimpleView.class)
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/image/modify/minor/", method = RequestMethod.PUT)
	public Image modifyLiteImage(@RequestBody Image image) throws CollectiblesException {
		Image imageResult = null;
		if (image.getId()!=null){
			Image imageInDB = imageRepository.findOne(image.getId());
			if (imageInDB!=null){
				imageInDB.setNotBook(image.getNotBook());
				imageResult = imageRepository.save(imageInDB);				
			}
		}
		return imageResult;
	} 
	
	private ResponseEntity<byte[]> generateImage(byte [] data){
		final HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.IMAGE_PNG);	    	    
		if (data!=null){
			headers.setCacheControl("no-transform,public,max-age=3600,s-maxage=3600");				
			
		    return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);			    
		}else {
			return new ResponseEntity<byte[]>(null, headers, HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value="/image/content/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getImageContent(@PathVariable String id) throws CollectiblesException{
		Long imageId = this.getId(id);
		if (imageId==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
		    byte [] data = null;
		    Image image = imageRepository.findOne(imageId);
		    if (image!=null){
		    	data = image.getData();
		    }
		    return generateImage(data);
		    
		}
	}
	
	@RequestMapping(value="/image/thumb/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getImageThumb(@PathVariable String id) throws CollectiblesException{
		Long imageId = this.getId(id);
		if (imageId==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			
			byte [] data = null;
		    Image image = imageRepository.findOne(imageId);
		    if (image!=null){
		    	data = image.getThumb();
		    }
		    return generateImage(data);
		}
	}
	
	@RequestMapping(value="/image/author/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getAuthorImage(@PathVariable String id) throws CollectiblesException{
		
		Author author = authorRepository.findOne(id);
		if (author==null){
			throw new NotFoundException();
		} else {
			return generateImage(author.getImageData());
		}
		
	}
	
	@RequestMapping(value="/images/{ids}", method = RequestMethod.GET)
	public Iterable<Image> getImages(@PathVariable List<String> ids) throws CollectiblesException{
		List<Long> imageIds = new ArrayList<>();
		int i=0;
		for (String id: ids){
			Long longId = this.getId(id);
			if (longId==null){
				throw new IncorrectParameterException(new String[]{"id["+i+"]"});
			}else {
				imageIds.add(longId);
			}
			
			i++;
		}
				
		
		Iterable<Image> images = imageRepository.findAll(imageIds);
				
	
		if (images==null || images.iterator()==null ||!images.iterator().hasNext()){
			throw new NotFoundException();
		} else {
			return images;
		}
		
	}
	
}

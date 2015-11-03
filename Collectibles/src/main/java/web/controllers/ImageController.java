package web.controllers;

import java.util.ArrayList;
import java.util.List;

import model.dataobjects.Image;
import model.persistence.ImageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotFoundException;

@RestController
public class ImageController extends CollectiblesController{

	@Autowired
	private ImageRepository imageRepository;
	
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

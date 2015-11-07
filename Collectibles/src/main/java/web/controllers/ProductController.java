package web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import model.dataobjects.Category;
import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.persistence.CategoryValueRepository;
import model.persistence.HierarchyRepository;
import model.persistence.ProductRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.GenericException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotFoundException;


@RestController
public class ProductController  extends CollectiblesController{

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryValueRepository categoryValueRepository;
	
	@Autowired
	private HierarchyRepository hierarchyRepository;

	@RequestMapping(value="/product/find/{id}")
	public Product product(@PathVariable String id) throws CollectiblesException {
		Long idLong = this.getId(id);
		
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			Product product = productRepository.findOne(idLong);
			if (product==null){
				throw new NotFoundException();
			} else {
				return product;
			}
		}
	}
	
	@RequestMapping(value="/product/create/from/file/{hierarchy}", method = RequestMethod.POST)
	public Collection<Product> addProductsFromFile(
			@PathVariable String hierarchy,
			@RequestPart("file") MultipartFile file) throws CollectiblesException {
		Long hierarchyId = this.getId(hierarchy);
			
		if (hierarchyId!=null){
			
			HierarchyNode hierarchyNode = hierarchyRepository.findOne(hierarchyId);
			if (hierarchyNode!=null){
				if (file!=null){
					Collection<Product> products = new ArrayList<>();
					
					InputStream inputStream = null;
					try {
						inputStream = file.getInputStream();					
						InputStreamReader reader = new InputStreamReader(inputStream);					
						CSVParser parser = CSVFormat.EXCEL.withHeader().parse(reader);
						
						List<SimpleDateFormat> formats = new ArrayList<>();
						formats.add(new SimpleDateFormat("yyyy-mm-dd"));
						formats.add(new SimpleDateFormat("yyyy"));
						
						
						for (CSVRecord record : parser.getRecords()){
							String name = record.get("name");
							String description = record.get("description");
							String owned = record.get("owned");
							String reference = record.get("reference");
							String date = record.get("date");
							Product product = new Product();
							if (name!=null && !"".equals(name.trim())){ product.setName(name); }
							if (description!=null && !"".equals(description.trim())){ product.setDescription(description); }
							if (reference!=null && !"".equals(reference.trim())){ product.setReference(reference); }
							if (owned !=null && owned.trim().equals("true")){product.setOwned(true); }
							if (date!=null && !date.trim().equals("")){
								for (SimpleDateFormat format : formats ){
									try {
										Date parsedDate = format.parse(date);
										if (parsedDate!=null){
											product.setReleaseDate(parsedDate);
										}
									} catch (ParseException e) {}
								}
								
							}
							product.setHierarchyPlacement(hierarchyNode);							
							validate(product);
							
							
							
							products.add(product);														
						}
						productRepository.save(products);
						return products;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new IncorrectParameterException(new String[]{"file"});
					}
					
				} else {
					throw new IncorrectParameterException(new String[]{"file"});
				}				
			} else {
				throw new NotFoundException(new String[]{"hierarchy"});
			}
		} else {
			throw new IncorrectParameterException(new String[]{"hierarchy"});
		}		
	}
	
	
	@RequestMapping(value="/product/create/{hierarchy}", method = RequestMethod.POST)
	public Product addProduct(@RequestBody Product product,@PathVariable String hierarchy) throws CollectiblesException {
		Long hierarchyId = this.getId(hierarchy);
		
		if (hierarchyId!=null){
			
			HierarchyNode hierarchyNode = hierarchyRepository.findOne(hierarchyId);
			if (hierarchyNode!=null){
				product.setId(null);
				product.setHierarchyPlacement(hierarchyNode);
				this.validate(product);				
				productRepository.save(product);
				return product;
			} else {
				throw new NotFoundException(new String[]{"hierarchy"});
			}
		} else {
			throw new IncorrectParameterException(new String[]{"hierarchy"});
		}		
		
	}
	
	@RequestMapping(value="/product/remove/{id}", method = RequestMethod.POST)
	public Long removeProduct(@PathVariable String id) throws CollectiblesException {		
		Long idLong = this.getId(id);
		
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {		
			try {
				productRepository.delete(idLong);
			}catch(EmptyResultDataAccessException ex) {				
				throw new NotFoundException(new String[]{"product"});
			}
			return idLong;
		}
	}
	
	
	@RequestMapping(value="/product/find/{productId}/category/value/add/{categoryValueId}", method = RequestMethod.POST)
	public Product addCategoryValue(@PathVariable String productId, @PathVariable String categoryValueId) throws CollectiblesException {
		Long longProductId = this.getId(productId);
		Long longCategoryValueId = this.getId(categoryValueId);
		List<String> errors = new ArrayList<>();
		
		if (longProductId==null) { errors.add("product"); }
		if (longCategoryValueId==null) { errors.add("categoryValue"); }
		if (errors.size()>0){
			throw new IncorrectParameterException(errors);
		}
		
		CategoryValue categoryValue = categoryValueRepository.findOne(longCategoryValueId);
		Product product = productRepository.findOne(longProductId);		
		if (product==null){ errors.add("product");}
		if (categoryValue==null){ errors.add("categoryValue");}
		if (errors.size()>0){
			throw new NotFoundException(errors);
		}
		
		Category category = categoryValue.getCategory();
		Collection<Category> productCategories = product.getHierarchyPlacement().getCategories();
		
		System.out.println(category);
		System.out.println(productCategories);
		
		if (productCategories == null || category == null || !productCategories.contains(category)){
			throw new IncorrectParameterException(new String[]{"category"});
		}
		
		product.addCategoryValue(categoryValue);
		productRepository.save(product);		
		System.out.println("HERE");
		return product;
	}
	
	@RequestMapping(value="/product/modify/", method = RequestMethod.PUT)
	public Product modifyProduct(@RequestBody Product product) throws CollectiblesException {		
		this.validate(product);
		
		if (product.getId()!=null){
			Product productInDb = productRepository.findOne(product.getId());
						
			if (productInDb!=null){				
				if ( 
						product.getHierarchyPlacement()==null || 
						product.getHierarchyPlacement().getId()==null || 
						hierarchyRepository.findOne(product.getHierarchyPlacement().getId())==null
				){				
					System.out.println("Resetting placement");					
					product.setHierarchyPlacement(productInDb.getHierarchyPlacement());
				} else {
					System.out.println("Conserving placement");
				}
			 				
				product.setCategoryValues(productInDb.getCategoryValues());
				product.setImages(productInDb.getImages());
				Product result = productRepository.save(product);
				return result;
			} else {
				throw new NotFoundException(new String[]{"product"});
			}
		} else {
			throw new IncorrectParameterException(new String[]{"product.id"});
		}			
	}
	
	@RequestMapping(value="/product/{id}/image/add/", method = RequestMethod.POST)
	public List<Image> addImageToProduct(@PathVariable String id,			
            @RequestPart("images") MultipartFile[] files)
            throws CollectiblesException {
		
		List<String> errors = this.validateImages(files);
		
		if (errors!=null && errors.size()>0){
			throw new IncorrectParameterException(errors);
		}
		
		Long productId = this.getId(id);
		if (productId!=null){
			Product productInDb = productRepository.findOne(productId);
			if (productInDb!=null){		
				
				List<Image> images = new ArrayList<>();
				int j=0;
				for (MultipartFile file : files) {
					Image image = new Image();	
					try {
						image.setData(file.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
						throw new IncorrectParameterException(new String[]{"images[" + j + "]"});				
					}
					images.add(image);
					j++;
				}
				
				productRepository.addImage(productInDb, images);
				return images;
			} else {
				throw new NotFoundException(new String[]{"product"});
			}			
		} else {
			throw new IncorrectParameterException(new String[]{"id"});
		}
	}
	
	@RequestMapping(value="/product/{productId}/image/remove/{imageId}", method = RequestMethod.POST)
	public Long removeImageFromProduct(@PathVariable String productId,@PathVariable String imageId) throws CollectiblesException{
		Long longProductId = this.getId(productId);
		Long longImageId = this.getId(imageId);
		
		List<String> errors = new ArrayList<>();
		if (longProductId == null) { errors.add("product.id"); }
		if (longImageId == null) { errors.add("image.id"); }
		
		if (errors.size()>0){
			throw new IncorrectParameterException(errors);
		} else {			
			Product product = productRepository.findOne(longProductId);
			if (product ==null) {
				throw new NotFoundException(new String[]{"product"});
			} else{				
				try {
					boolean result = productRepository.removeImage(product,longImageId);
					if (result){
						return longImageId;
					} else {
						throw new NotFoundException(new String[]{"product.image"});
					}
				}catch(EmptyResultDataAccessException ex) {				
					throw new NotFoundException(new String[]{"image"});
				}
				
			}
			
		}
		
	}
	
	private List<String> validateImages(MultipartFile[] files){
		List<String> errors = new ArrayList<>();
		if (files==null || files.length<=0){
			errors.add("images");
			return errors;										
		} else {								
			int i=0;
			for (MultipartFile file : files) {
				if (file.getContentType()==null || !file.getContentType().startsWith("image")){
					errors.add("images[" + i + "].content-type");					
				}
				i++;
			}
		}
		return errors;
	}
}

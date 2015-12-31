package web.controllers.form;

import java.util.ArrayList;
import java.util.List;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.User;
import model.persistence.UserRepository;
import model.persistence.queryParameters.ProductSearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import web.supporting.error.exceptions.IncorrectParameterException;

public class SearchForm {
	
	private static final Logger logger = LoggerFactory.getLogger(SearchForm.class);
	
	
	private HierarchyNode hierarchy; 
	private String search;
	private String withImages;
	private String withPrice;
	private String owned;
	private String ownedBy;
	private String wished;
	private String wishedBy;
	private String store;
	private String seller;
	private List<String> categories;
	private String sortBy;
	private String sortOrder;
	private Integer page;
	private Integer maxResults;
	public HierarchyNode getHierarchy() {
		return hierarchy;
	}
	public void setHierarchy(HierarchyNode hierarchy) {
		this.hierarchy = hierarchy;
	}
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public String getWithImages() {
		return withImages;
	}
	public void setWithImages(String withImages) {
		this.withImages = withImages;
	}
	public String getWithPrice() {
		return withPrice;
	}
	public void setWithPrice(String withPrice) {
		this.withPrice = withPrice;
	}
	public String getOwned() {
		return owned;
	}
	public void setOwned(String owned) {
		this.owned = owned;
	}
	public String getOwnedBy() {
		return ownedBy;
	}
	public void setOwnedBy(String ownedBy) {
		this.ownedBy = ownedBy;
	}
	public String getWished() {
		return wished;
	}
	public void setWished(String wished) {
		this.wished = wished;
	}
	public String getWishedBy() {
		return wishedBy;
	}
	public void setWishedBy(String wishedBy) {
		this.wishedBy = wishedBy;
	}
	public String getStore() {
		return store;
	}
	public void setStore(String store) {
		this.store = store;
	}
	public String getSeller() {
		return seller;
	}
	public void setSeller(String seller) {
		this.seller = seller;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public String getSortBy() {
		return sortBy;
	}
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getMaxResults() {
		return maxResults;
	}
	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}
	
	public ProductSearch convertToDbRequest(UserRepository userRepository) throws IncorrectParameterException{

		Boolean withImages = null;			
		Boolean withPrice = null;

		boolean errorReturnNoResults = false;
		ProductSearch searchObject = new ProductSearch();
		
		searchObject.setSearchTerm(this.getSearch());									
		searchObject.setPage(this.getPage());
		searchObject.setMaxResults(this.getMaxResults());
		

		Boolean owned = null;
					
		if ("true".equals(this.getOwned())){
			owned = Boolean.TRUE;
		} else if ("false".equals(this.getOwned())){
			owned = Boolean.FALSE;
		}
					
		if (owned!=null){
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();				
			User owner = null;
			if (auth!=null){
				owner = userRepository.findOne(auth.getName());
			}
			
			if (owner!=null){
				if (owned){ 
					searchObject.addUsersWhoOwn(owner); 
				} else {
					searchObject.addUsersWhoDontOwn(owner); 
				}
			} else {
				logger.info("Skipping owned parameter due to user probably has been logged out");					
			}
		}
		
		if (this.getOwnedBy()!=null && !this.getOwnedBy().trim().equals("")){
			Long userId = null;
			try {
				userId = Long.parseLong(this.getOwnedBy());
			}catch (Exception e){
				logger.error("Exception reading user id as long for search", e);
			}
			if (userId!=null){
				User user = userRepository.findById(userId);				
				if (user!=null){
					searchObject.addUsersWhoOwn(user);
					if (searchObject.getUsersWhoDontOwn()!=null && searchObject.getUsersWhoDontOwn().contains(user)){
						logger.info("Setting error due to search for who owns and doesnt own");
						errorReturnNoResults = true;
					}
				} else {
					logger.info("Setting error due to user not existing");
					errorReturnNoResults = true;
				}
			}
		}
					
		Boolean wished = null;
		
		if ("true".equals(this.getWished())){
			wished = Boolean.TRUE;
		} 
		
		if (wished!=null){
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();				
			User wisher = null;
			if (auth!=null){
				wisher = userRepository.findOne(auth.getName());
			}
			
			if (wisher!=null){
				if (wished){ 
					searchObject.addWisher(wisher); 
				} 
			} else {
				logger.info("Skipping owned parameter due to user probably has been logged out");					
			}
		}
		
		if (this.getWishedBy()!=null && !this.getWishedBy().trim().equals("")){
			Long userId = null;
			try {
				userId = Long.parseLong(this.getWishedBy());
			}catch (Exception e){
				logger.error("Exception reading user id as long for search", e);
			}
			if (userId!=null){
				User user = userRepository.findById(userId);				
				if (user!=null){
					searchObject.addWisher(user);
				} else {
					logger.info("Setting error due to user not existing");
					errorReturnNoResults = true;
				}
			}
		}
	
		
		if ("true".equals(this.getWithImages())){
			withImages = Boolean.TRUE;
		} else if ("false".equals(this.getWithImages())){
			withImages = Boolean.FALSE;
		}
							
		searchObject.setWithImages(withImages);
	

		if ("true".equals(this.getWithPrice())){
			withPrice = Boolean.TRUE;
		} else if ("false".equals(this.getWithPrice())){
			withPrice = Boolean.FALSE;
		}
		
		searchObject.setWithPrice(withPrice);
		
		searchObject.setStore(this.getStore());
		searchObject.setSeller(this.getSeller());
		
		
		searchObject.setHierarchy(this.getHierarchy());
		
					
		List<CategoryValue> categoryValues = new ArrayList<>();
		if (this.getCategories()!=null){
			for (String categoryValueString : this.getCategories()){
				Long categoryId = null;
				try  {
					categoryId = Long.parseLong(categoryValueString);
				} catch(Exception ex){}
				if (categoryId==null){
					throw new IncorrectParameterException(new String[]{"categories"});
				} else {
					CategoryValue categoryValue = new CategoryValue();
					categoryValue.setId(categoryId);
					categoryValues.add(categoryValue);
				}
			}
		}
		
		searchObject.setCategoryValues(categoryValues);		
		searchObject.setSortBy(this.getSortBy());
		searchObject.setSortOrder(this.getSortOrder());
		
		if (errorReturnNoResults){
			return null;
		}
		return searchObject;
		
	}
}

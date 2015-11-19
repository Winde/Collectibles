package model.persistence.queryParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.User;

public class ProductSearch implements SearchObject {
	private HierarchyNode hierarchy = null;	
	private String searchTerm = null;	
	private Collection<CategoryValue> categoryValues = null;	
	private Boolean withImages = null;	
	private Set<User> usersWhoOwn = null;
	private Set<User> usersWhoDontOwn = null;
	private Set<User> wishers = null;
	private Integer maxResults = null;
	private Integer page = null;
	private Boolean withPrice = null;
	private String sortBy = null;
	private String sortOrder = null;
	
	public ProductSearch(){}

	public HierarchyNode getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(HierarchyNode hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public Collection<CategoryValue> getCategoryValues() {
		return categoryValues;
	}

	public void setCategoryValues(Collection<CategoryValue> categoryValues) {
		this.categoryValues = categoryValues;
	}

	public Boolean getWithImages() {
		return withImages;
	}

	public void setWithImages(Boolean withImages) {
		this.withImages = withImages;
	}

	public Set<User> getUsersWhoOwn() {
		return usersWhoOwn;
	}

	public void setUsersWhoOwn(Set<User> usersWhoOwn) {
		this.usersWhoOwn = usersWhoOwn;
	}

	public Set<User> getUsersWhoDontOwn() {
		return usersWhoDontOwn;
	}

	public void setUsersWhoDontOwn(Set<User> usersWhoDontOwn) {
		this.usersWhoDontOwn = usersWhoDontOwn;
	}

	public void addUsersWhoOwn(User user){
		if (this.usersWhoOwn==null){
			this.usersWhoOwn = new HashSet<>();
		}
		this.usersWhoOwn.add(user);
	}
	
	public void addUsersWhoDontOwn(User user){
		if (this.usersWhoDontOwn==null){
			this.usersWhoDontOwn = new HashSet<>();
		}
		this.usersWhoDontOwn.add(user);
	}
	
	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Boolean getWithPrice() {
		return withPrice;
	}

	public void setWithPrice(Boolean withPrice) {
		this.withPrice = withPrice;
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
	
	public Set<User> getWishers() {
		return wishers;
	}

	public void setWishers(Set<User> wishers) {
		this.wishers = wishers;
	}
	
	public void addWisher(User user) {
		if (this.wishers==null){
			this.wishers = new HashSet<>();
		}
		this.wishers.add(user);		
	}

	@Override
	public Collection<String> errors() {
		Collection<String> errors = new ArrayList<>();
		if (hierarchy==null && searchTerm==null){
			errors.add("hierarchy");
		}

		return errors;
	}



	


}

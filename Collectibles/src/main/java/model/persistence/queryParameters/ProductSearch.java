package model.persistence.queryParameters;

import java.util.ArrayList;
import java.util.Collection;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.User;

public class ProductSearch implements SearchObject {
	private HierarchyNode hierarchy = null;	
	private String searchTerm = null;	
	private Collection<CategoryValue> categoryValues = null;	
	private Boolean withImages = null;
	private Boolean owned = null;
	private User owner = null;
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

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
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
	
	public Boolean getOwned() {
		return owned;
	}

	public void setOwned(Boolean owned) {
		this.owned = owned;
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

	@Override
	public Collection<String> errors() {
		Collection<String> errors = new ArrayList<>();
		if (hierarchy==null && searchTerm==null){
			errors.add("hierarchy");
		}

		return errors;
	}

	


}

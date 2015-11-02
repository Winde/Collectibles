package model.dataobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class Product extends SimpleIdDao{


	@ManyToOne
	@JsonIgnoreProperties({ "father", "children"})
	private HierarchyNode hierachyPlacement;
	
	@OneToMany
	private Set<CategoryValue> categoryValues;
	
	@Column(unique=true )
	private String reference = null;
	
	@Column
	private String description = null;
	
	@Column
	private String imagePath = null;
	
	@Column
	private String name = null;


		
	public Product(){}

	public String getReference() {
		return reference;
	}


	public void setReference(String reference) {
		this.reference = reference;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getImagePath() {
		return imagePath;
	}


	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public HierarchyNode getHierachyPlacement() {
		return hierachyPlacement;
	}

	public void setHierachyPlacement(HierarchyNode hierachyPlacement) {
		this.hierachyPlacement = hierachyPlacement;
	}

	public Set<CategoryValue> getCategoryValues() {
		if (categoryValues == null){
			return null;
		} else {
			return new TreeSet<>(categoryValues);
		}
	}

	public void setCategoryValues(Set<CategoryValue> categoryValue) {
		this.categoryValues = categoryValue;
	}
	
	public boolean addCategoryValue(CategoryValue categoryValue) {
		return categoryValues.add(categoryValue);
	}
	
	public boolean removeCategoryValue(CategoryValue categoryValue) {
		return categoryValues.remove(categoryValue);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}

package model.dataobjects;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class Category extends SimpleIdDao{

	@Column(unique=true)
	private String name;
	
	@OneToMany(mappedBy="category", cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"category"})
	private Set<CategoryValue> categoryValues;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@JsonIgnoreProperties({ "father", "children", "categories" })
	private Set<HierarchyNode> hierarchies;

	public Category(){
		hierarchies = new TreeSet<>();
		categoryValues = new TreeSet<>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Collection<CategoryValue> getCategoryValues() {
		if (categoryValues==null){
			return null;
		} else {
			return new TreeSet<>(categoryValues);
		}
	}
	
	public boolean addCategoryValue(CategoryValue categoryValue){
		return this.categoryValues.add(categoryValue);
	}
	
	public boolean removeCategoryValue(CategoryValue categoryValue){
		return this.categoryValues.remove(categoryValue);
	}
	
	public Collection<HierarchyNode> getHierarchies() {
		return hierarchies;
	}
	public boolean addHierarchy(HierarchyNode hierarchyNode) {	
		return hierarchies.add(hierarchyNode);
	}
	
	public boolean removeHierarchy(HierarchyNode hierarchyNode) {
		if (hierarchies==null){
			return false;
		}
		return hierarchies.remove(hierarchyNode);
	}
	
	public String toString(){
		return "{ id: " + this.getId() + " ,name: " + this.getName() + " }";
	}
	
}

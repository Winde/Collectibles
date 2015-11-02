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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
public class HierarchyNode extends SimpleIdDao{

	@Column
	private String name;
	
	@OneToMany	
	private Set<HierarchyNode> children;
	
	@ManyToOne
	@JsonIgnoreProperties({ "father", "children", "categories"})
	private HierarchyNode father;
		
	@ManyToMany	
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private Set<Category> categories; 
	
	public HierarchyNode(){
		children = new TreeSet<>();
		categories = new TreeSet<>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Collection<HierarchyNode> getChildren(){
		if (children==null){
			return null;
		} else {
			return new ArrayList<>(children);
		}
	}
	
	public boolean addChildren(HierarchyNode node){
		if (this.getId()==null || node.getId()==null || !this.getId().equals(node.getId())) {
			return children.add(node);			
		} 
		return false;
	}
	
	public boolean removeChildren(HierarchyNode node){
		if (children == null) {
			return false;
		} else {
			return children.remove(node);			
		}		
	}
	
	public HierarchyNode getFather() {
		return father;
	}
	
	public void setFather(HierarchyNode father) {
		this.father = father;
	}
	
	public Collection<Category> getCategories(){
		if (categories==null){
			return null;
		} else {
			return new ArrayList<>(categories);
		}
	}
	
	public boolean addCategory(Category category){
		return categories.add(category);
	}
	
	public boolean removeCategory(Category category){
		if (categories==null){
			return false;
		} else {
			return categories.remove(category);
		}
	}


}

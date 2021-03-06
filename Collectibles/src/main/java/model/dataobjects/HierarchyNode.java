package model.dataobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;


@Entity(name="HierarchyNode")
public class HierarchyNode extends SimpleIdDao{

	public interface HierarchySimpleView extends SimpleIdDaoView{};
	public interface HierarchyTreeView extends HierarchySimpleView{};
	public interface HierarchyComplexView extends HierarchyTreeView {};
	
	
	@Column(name="name")
	@JsonView(HierarchySimpleView.class)
	private String name;
	
	@OneToMany	
	@JsonIgnoreProperties({"father","categories"})	
	@JsonView(HierarchyTreeView.class)
	private Set<HierarchyNode> children;
	
	@ManyToOne
	@JsonIgnoreProperties({"children", "categories"})
	@JsonView(HierarchyComplexView.class)
	private HierarchyNode father;

	@ManyToMany(fetch=FetchType.LAZY)	
	//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	//@JsonView(HierarchyComplexView.class)
	@JsonIgnore
	private Set<Category> categories; 

	@Column(name="lineage")
	@JsonView(HierarchySimpleView.class)
	private String lineage;
	
	@Column(name="depth")
	@JsonView(HierarchySimpleView.class)
	private Integer depth;
		
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="hierarchy_connectors", joinColumns=@JoinColumn(name="id"))
	@Column(name="hierarchy_connectors")	
	@BatchSize(size = 50)
	@JsonView(HierarchyTreeView.class)
	private Set<String> connectorNames;
		
	@Column(name="is_book")
	@JsonView(HierarchySimpleView.class)
	private Boolean isBook;
	
	public HierarchyNode(){
		children = new TreeSet<>();
		//categories = new TreeSet<>();
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
	
	public boolean emptyChildren(){
		if (this.children!=null){
			this.children.clear();
		}
		return true;
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
		return categories;
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

	public String getLineage() {
		return lineage;
	}

	public Set<String> getConnectorNames() {
		return connectorNames;
	}

	public void setConnectorNames(Set<String> connectorNames) {
		this.connectorNames = connectorNames;
	}

	public boolean updateLineage() {
		if (this.getFather()!=null && this.getFather().getLineage()!=null && this.getId()!=null){
			this.lineage = this.getFather().getLineage() + "-" + this.getId();
			if (this.getFather().getDepth()!=null){
				this.depth = this.getFather().getDepth()+1;
			}
			return true;
		} else {
			return false;
		}
	}

	public Integer getDepth() {
		return depth;
	}

	public Boolean getIsBook() {
		return isBook;
	}

	public void setIsBook(Boolean isBook) {
		this.isBook = isBook;
	}

}

package model.dataobjects.serializable;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.BeanUtils;

import model.connection.ProductInfoConnector;
import model.connection.ProductInfoConnectorFactory;
import model.dataobjects.Category;
import model.dataobjects.HierarchyNode;
import model.dataobjects.HierarchyNode.HierarchyTreeView;
import model.dataobjects.SimpleIdDao.SimpleIdDaoView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

public class SerializableHierarchyNode {

	public interface HierarchySimpleView extends SimpleIdDaoView{};
	public interface HierarchyTreeView extends HierarchySimpleView{};
	public interface HierarchyComplexView extends HierarchyTreeView {};

	@JsonView(HierarchySimpleView.class)
	private Long id;
	
	@JsonView(HierarchySimpleView.class)
	private String name;
	
	@JsonIgnoreProperties({"father","categories"})	
	@JsonView(HierarchyTreeView.class)
	private Set<SerializableHierarchyNode> children;
	
	@JsonIgnoreProperties({"children", "categories"})
	@JsonView(HierarchyComplexView.class)
	private SerializableHierarchyNode father;

	@JsonIgnore
	private Set<Category> categories; 

	@JsonView(HierarchySimpleView.class)
	private String lineage;
	
	@JsonView(HierarchySimpleView.class)
	private Integer depth;
		
	@JsonView(HierarchyTreeView.class)
	private SortedSet<ProductInfoConnector> connectors;
		
	@JsonView(HierarchySimpleView.class)
	private Boolean isBook;

	public SerializableHierarchyNode(){
		
	}
	

	public SerializableHierarchyNode(HierarchyNode node, ProductInfoConnectorFactory connectorFactory){
		createSerializable(node,connectorFactory);
	}
	
	private SerializableHierarchyNode createSerializable(HierarchyNode node, ProductInfoConnectorFactory connectorFactory){
		BeanUtils.copyProperties(node, this, new String[]{"children","father"});
		this.children = new HashSet<>();
		if (node.getConnectorNames()!=null){
			connectors = new TreeSet<>();
			for (String connectorName : node.getConnectorNames()){
				ProductInfoConnector connector = connectorFactory.getConnector(connectorName);	
				if (connector!=null){
					connectors.add(connector);
				}
			}
		}
		for (HierarchyNode child : node.getChildren()){
			SerializableHierarchyNode childSerializable = new SerializableHierarchyNode();
			childSerializable = childSerializable.createSerializable(child,connectorFactory);
			childSerializable.setFather(this);
			this.children.add(childSerializable);
		}
		return this;
	}

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<SerializableHierarchyNode> getChildren() {
		return children;
	}

	public void setChildren(Set<SerializableHierarchyNode> children) {
		this.children = children;
	}

	public SerializableHierarchyNode getFather() {
		return father;
	}

	public void setFather(SerializableHierarchyNode father) {
		this.father = father;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}

	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		this.lineage = lineage;
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	public SortedSet<ProductInfoConnector> getConnectors() {
		return connectors;
	}


	public void setConnectors(SortedSet<ProductInfoConnector> connectors) {
		this.connectors = connectors;
	}


	public Boolean getIsBook() {
		return isBook;
	}

	public void setIsBook(Boolean isBook) {
		this.isBook = isBook;
	}
	
	
	
}

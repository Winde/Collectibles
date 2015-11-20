package model.dataobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import model.connection.ProductInfoConnector;
import model.connection.ProductInfoConnectorFactory;
import model.connection.TooFastConnectionException;
import model.dataobjects.events.ProductSaveListener;

import org.hibernate.annotations.BatchSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity(name="Product")
@EntityListeners(ProductSaveListener.class)
public class Product extends SimpleIdDao{

	private static final Logger logger = LoggerFactory.getLogger(Product.class);
	
	private static final long MINIMUM_LENGTH_DESCRIPTION = 100;
	
	@ManyToOne	
	private HierarchyNode hierarchyPlacement;
	
	@OneToMany(fetch=FetchType.LAZY)
	@BatchSize(size = 20)
	private Set<CategoryValue> categoryValues;
	
	@Column(name="reference",unique=true )
	private String reference = null;
	
	@Column(name="description")
	@Lob @Basic	
	private String description = null;

	@Column(name="name")
	private String name = null;

	@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.MERGE,CascadeType.REMOVE})
	@BatchSize(size = 50)
	private List<Image> images = null;

	@ManyToMany(fetch=FetchType.LAZY)	
	@BatchSize(size = 50)
	private Set<User> owners = null;
	
	@ManyToMany(fetch=FetchType.LAZY)	
	@BatchSize(size = 50)
	private Set<User> ownersOtherLanguage = null;
	
	@ManyToMany(fetch=FetchType.LAZY)	
	@BatchSize(size = 50)
	private Set<User> wishers = null;	
	
	@Column(name="release_date")
	private Date releaseDate = null;
			
	@Column(name="universal_reference")
	private String universalReference = null;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="connector_references", joinColumns=@JoinColumn(name="id"))
	@Column(name="connector_reference")	
	@BatchSize(size = 50)
	private Map<String,String> connectorReferences;
		
	@Column(name="last_price_update")
	private Date lastPriceUpdate = null;
	
	@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.MERGE})
	private Set<Author> authors;

	@Column(name="publisher")
	private String publisher;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="processed_connectors", joinColumns=@JoinColumn(name="id"))
	@Column(name="connector")	
	private Collection<String> processedConnectors;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="product_dollar_price", joinColumns=@JoinColumn(name="id"))
	@Column(name="dollar_price")
	private Map<String,Long> dollarPrice = null;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="product_rating", joinColumns=@JoinColumn(name="id"))
	@Column(name="rating")
	private Map<String,Double> ratings = null;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="product_links", joinColumns=@JoinColumn(name="id"))
	@Column(name="external_links")
	private Map<String,String> externalLinks = null;
	
	
	@Column(name="min_price")
	private Long minPrice = null;	

	public Product(){
		dollarPrice = new HashMap<>();
	}
	
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

	public HierarchyNode getHierarchyPlacement() {
		return hierarchyPlacement;
	}

	public void setHierarchyPlacement(HierarchyNode hierarchyPlacement) {
		this.hierarchyPlacement = hierarchyPlacement;
	}

	public Set<CategoryValue> getCategoryValues() {
		return categoryValues;		
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
	
	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images){
		this.images = images;
	}
	
	public boolean addImage(Image image) {
		if (this.images==null){
			this.images = new ArrayList<>();
		} 
		return this.images.add(image);
	}
	
	public boolean removeImage(Image image) {
		if (this.images==null){
			return false;
		} else {
			return this.images.remove(image);
		}
	}

	public Set<User> getOwners() {
		return owners;
	}

	public void setOwners(Set<User> owners) {
		this.owners = owners;
	}

	public Set<User> getOwnersOtherLanguage() {
		return ownersOtherLanguage;
	}

	public void setOwnersOtherLanguage(Set<User> ownersOtherLanguage) {
		this.ownersOtherLanguage = ownersOtherLanguage;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	
	@JsonIgnoreProperties({ "data" })	
	public Image getMainImage(){
		if (this.images==null || this.getImages().size()<=0){
			return null;
		} else {
			for (Image image: this.images){
				if (image.isMain()){
					return image;
				}
			}
			return null;
		}
	}

	public String getUniversalReference() {
		return universalReference;
	}

	public void setUniversalReference(String universalReference) {
		this.universalReference = universalReference;
	}

	public Map<String,String> getConnectorReferences() {
		return connectorReferences;
	}

	public void setConnectorReferences(Map<String,String> connectorReferences) {
		this.connectorReferences = connectorReferences;
	}
	
	public Set<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<Author> authors) {
		this.authors = authors;
	}

	public String toString(){
		return "{" + this.getId() + " - " + this.getName() +"}";		
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public void addConnector(String connector){
		if (this.getProcessedConnectors()==null){
			this.setProcessedConnectors(new ArrayList<>());
		}
		this.getProcessedConnectors().add(connector);
	}
	
	public Collection<String> getProcessedConnectors() {		
		return processedConnectors;
	}

	public void setProcessedConnectors(Collection<String> processedConnectors) {
		this.processedConnectors = processedConnectors;
	}

	public Map<String, Long> getDollarPrice() {
		return dollarPrice;
	}

	public void setDollarPrice(Map<String, Long> dollarPrice) {
		this.dollarPrice = dollarPrice;
	}

	public void setDollarPrice(String provider, Long dollarPrice) {
		Map<String, Long> map = this.getDollarPrice();
		if (map == null){
			map = new HashMap<>();
			this.setDollarPrice(map);
		}
		map.put(provider, dollarPrice);
		
		if (dollarPrice!=null){
			this.setLastPriceUpdate(new Date());
		}
		
		if (dollarPrice!=null && (minPrice == null || dollarPrice < minPrice)){
			this.setMinPrice(dollarPrice);
		}
	}	
	
	public Long getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Long minPrice) {
		this.minPrice = minPrice;
	}

	public boolean isLengthyDescription() {
		if (this.getDescription()==null){
			return false;
		}else if(this.getDescription().length()<MINIMUM_LENGTH_DESCRIPTION){
			return false;
		} else {
			return true;
		}
	}

	public synchronized boolean updateWithConnector(ProductInfoConnector connector) throws TooFastConnectionException {
		return connector.updateProductTransaction(this);
	}

	public synchronized boolean updatePricesWithConnector(ProductInfoConnector connector) throws TooFastConnectionException {
		return connector.updatePriceTransaction(this);
	}

	public Date getLastPriceUpdate() {
		return lastPriceUpdate;
	}

	public void setLastPriceUpdate(Date lastPriceUpdate) {
		this.lastPriceUpdate = lastPriceUpdate;
	}

	public Map<String, Double> getRatings() {
		return ratings;
	}

	public void setRatings(Map<String, Double> ratings) {
		this.ratings = ratings;
	}

	public Map<String, String> getExternalLinks() {		
		return externalLinks;
	}

	public void setExternalLinks(Map<String, String> externalLinks) {
		this.externalLinks = externalLinks;
	}

	public Set<User> getWishers() {
		return wishers;
	}

	public void setWishers(Set<User> wishers) {
		this.wishers = wishers;
	}

}


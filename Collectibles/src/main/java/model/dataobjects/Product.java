package model.dataobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;

import model.connection.ProductInfoConnector;
import model.connection.TooFastConnectionException;
import model.dataobjects.Author.AuthorView;
import model.dataobjects.HierarchyNode.HierarchySimpleView;
import model.dataobjects.Image.ImageSimpleView;
import model.dataobjects.SimpleIdDao.SimpleIdDaoView;
import model.dataobjects.supporting.ObjectList.ObjectListView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

@Entity(name="Product")
public class Product extends SimpleIdDao{

	private static final long MINIMUM_LENGTH_DESCRIPTION = 100;
	
	@ManyToOne	
	private HierarchyNode hierarchyPlacement;
	
	@OneToMany(fetch=FetchType.LAZY)
	private Set<CategoryValue> categoryValues;
	
	@Column(name="reference",unique=true )
	private String reference = null;
	
	@Column(name="description")
	@Lob @Basic	
	private String description = null;

	@Column(name="name")
	private String name = null;

	@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.MERGE,CascadeType.REMOVE})
	private List<Image> images = null;

	@ManyToMany(fetch=FetchType.LAZY)	
	private Set<User> owners = null;	
	
	@Column(name="release_date")
	private Date releaseDate = null;
			
	@Column(name="universal_reference")
	private String universalReference = null;
	
	@Column(name="goodreads_reference")
	private String goodreadsReference = null;
	
	@Column(name="last_price_update")
	private Date lastPriceUpdate = null;
	
	@Column(name="amazon_url")	
	private String amazonUrl = null;
	
	@Column(name="goodreads_url")
	private String goodreadsUrl = null;

	@Column(name="drivethrurpg_url")
	private String drivethrurpgUrl = null;

	
	@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.MERGE})
	private Set<Author> authors;
	
	@Column(name="goodreads_related_link")
	private String goodreadsRelatedLink;
	
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
		if (this.getUniversalReference()!=null && !this.getUniversalReference().equals(universalReference)){
			if (this.getProcessedConnectors()!=null){
				this.getProcessedConnectors().clear();
			}
		}
		this.universalReference = universalReference;
	}

	public String getAmazonUrl() {
		return amazonUrl;
	}

	public void setAmazonUrl(String amazonUrl) {
		this.amazonUrl = amazonUrl;
	}
	
	public Set<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<Author> authors) {
		this.authors = authors;
	}

	public String getGoodreadsRelatedLink() {
		return goodreadsRelatedLink;
	}

	public void setGoodreadsRelatedLink(String goodreadsRelatedLink) {
		this.goodreadsRelatedLink = goodreadsRelatedLink;
	}

	public String getGoodreadsUrl() {
		return goodreadsUrl;
	}

	public void setGoodreadsUrl(String goodreadsUrl) {
		this.goodreadsUrl = goodreadsUrl;
	}
	
	

	public String getDrivethrurpgUrl() {
		return drivethrurpgUrl;
	}

	public void setDrivethrurpgUrl(String drivethrurpgUrl) {
		this.drivethrurpgUrl = drivethrurpgUrl;
	}

	public String toString(){
		return "{" + this.getId() + " - " + this.getName() + " - " + this.getDescription() +"}";		
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

	public String getGoodreadsReference() {
		return goodreadsReference;
	}

	public void setGoodreadsReference(String goodreadsReference) {
		this.goodreadsReference = goodreadsReference;
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
	
	
}


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
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

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
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
	
	@Column(name="description")
	@Lob @Basic	
	private String description = null;

	@Column(name="name")
	private String name = null;

	//@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.MERGE,CascadeType.REMOVE})
	@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.ALL})
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
	@CollectionTable(name="product_connector_references", joinColumns=@JoinColumn(name="id"))
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
	
	@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.ALL})
	@OrderBy("price")
	private SortedSet<Price> prices = null;
		
	@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.ALL})
	@OrderBy("rating")
	private SortedSet<Rating> ratings = null;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="product_links", joinColumns=@JoinColumn(name="id"))
	@Column(name="external_links")
	@MapKeyColumn(name="external_links_key")
	@OrderBy("external_links_key")
	private SortedMap<String, String> externalLinks = null;
		
	@Column(name="min_price")
	private Long minPrice = null;	

	@Column(name="min_price_link")
	private String minPriceLink = null;
	
	@Column(name="min_price_seller")
	private String minPriceSeller = null;
		
	@Column(name="main_rating")
	private Double mainRating = null;
	
	public Product(){		
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
		image.setProduct(this);
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

 
	public void setPrices(SortedSet<Price> prices) {
		this.prices = prices;		
	}
	
	public SortedSet<Price> getPrices() {
		return prices;
	}

	public void removePrice(String provider){		
		logger.info("Cleaning prices for "+provider+": " + this.getPrices());
		if (provider!=null){
			if (this.getPrices()!=null){
				Iterator<Price> iterator = this.getPrices().iterator();
				while (iterator.hasNext()){
					Price priceInSet = iterator.next();
					if (provider.equals(priceInSet.getConnectorName())){
						iterator.remove();
					}
				}
			}
			this.calculateMinPrice();						
		}		
		logger.info("Cleaned prices for "+provider+": " + this.getPrices());
	}
	
	
	public void addPrice(Price price){	
		if (this.getPrices()==null){
			this.prices = new TreeSet<>();
		}
		if (price!=null && price.getConnectorName()!=null){
			prices.add(price);
			this.calculateMinPrice();
			logger.info("Calculated min price: " + this.minPrice);
		}				
	}
	

	public SortedSet<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(SortedSet<Rating> ratings) {
		this.ratings = ratings;		
	}
	
	public Rating removeRating(String provider){		
		Rating removed = null;
		if (provider!=null){
			Iterator<Rating> iterator = this.getRatings().iterator();
			while (iterator.hasNext()){
				Rating ratingInSet = iterator.next();
				if (provider.equals(ratingInSet.getProvider())){
					iterator.remove();
					removed = ratingInSet;
				}
			}
			this.mainRating = this.calculateBestRating();
		}
		return removed;
		
	}
	
	
	public Rating addRating(Rating rating){
		Rating removed = null;
		if (this.getRatings()==null){
			this.ratings = new TreeSet<>();
		}
		if (rating.getProvider()!=null){
			removed = this.removeRating(rating.getProvider());
			ratings.add(rating);
			this.mainRating = this.calculateBestRating();
		}		
		return removed;
	}
	
	public Double calculateBestRating(){
		if (this.getRatings()!=null && this.getRatings().size()>0){
			return this.getRatings().first().getRating();
		} else{
			return null;
		}
	}

	public void calculateMinPrice() {
		Price newMinPrice = null;
		if (prices!=null && prices.size()>0){			
			newMinPrice = prices.first();
			this.minPrice = newMinPrice.getPrice();
			this.minPriceLink = newMinPrice.getLink();
			this.minPriceSeller = newMinPrice.getSeller();
		} else {
			this.minPrice = null;
			this.minPriceLink = null;
			this.minPriceSeller = null;
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

	public Date getLastPriceUpdate() {
		return lastPriceUpdate;
	}

	public void setLastPriceUpdate(Date lastPriceUpdate) {
		this.lastPriceUpdate = lastPriceUpdate;
	}

	public SortedMap<String, String> getExternalLinks() {		
		return externalLinks;
	}

	public void setExternalLinks(SortedMap<String, String> externalLinks) {
		this.externalLinks = externalLinks;
	}

	public Set<User> getWishers() {
		return wishers;
	}

	public void setWishers(Set<User> wishers) {
		this.wishers = wishers;
	}

	public Double getMainRating() {
		return mainRating;
	}

	public void setMainRating(Double mainRating) {
		this.mainRating = mainRating;
	}

	public String getMinPriceLink() {
		return minPriceLink;
	}

	public void setMinPriceLink(String minPriceLink) {
		this.minPriceLink = minPriceLink;
	}

	public String getMinPriceSeller() {
		return minPriceSeller;
	}

	public void setMinPriceSeller(String minPriceSeller) {
		this.minPriceSeller = minPriceSeller;
	}

}


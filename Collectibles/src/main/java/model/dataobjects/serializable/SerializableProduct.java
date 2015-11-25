package model.dataobjects.serializable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import model.dataobjects.Author;
import model.dataobjects.Author.AuthorView;
import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Rating;
import model.dataobjects.HierarchyNode.HierarchySimpleView;
import model.dataobjects.Image;
import model.dataobjects.Image.ImageSimpleView;
import model.dataobjects.Product;
import model.dataobjects.SimpleIdDao.SimpleIdDaoView;
import model.dataobjects.User;
import model.dataobjects.supporting.ObjectList.ObjectListView;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

public class SerializableProduct {

	private static final Logger logger = LoggerFactory.getLogger(SerializableProduct.class);
	
	public interface ProductSimpleView  extends SimpleIdDaoView{};
	public interface ProductComplexView extends ProductSimpleView,AuthorView {};
	public interface ProductListView extends ObjectListView,ProductSimpleView,HierarchySimpleView,ImageSimpleView{};

	@JsonView(SimpleIdDaoView.class)
	public Long id;
	
	@JsonIgnoreProperties({"children"})
	@JsonView(ProductSimpleView.class)
	private HierarchyNode hierarchyPlacement;
	
	@JsonIgnore
	private Set<CategoryValue> categoryValues;
	
	@JsonView(ProductSimpleView.class)
	private String reference = null;
	
	@JsonView(ProductSimpleView.class)
	private String description = null;

	@JsonView(ProductSimpleView.class)
	private String name = null;

	@JsonIgnoreProperties({ "data" })
	@JsonView(ProductSimpleView.class)
	private List<Image> images = null;

	@JsonView(ProductSimpleView.class)
	private Boolean owned = Boolean.FALSE;	

	@JsonView(ProductSimpleView.class)
	private Boolean ownedAnotherLanguage = Boolean.FALSE;
	
	@JsonView(ProductSimpleView.class)
	private Boolean wished = Boolean.FALSE;	
	
	
	@JsonView(ProductSimpleView.class)
	private Date releaseDate = null;
			
	@JsonView(ProductSimpleView.class)
	private String universalReference = null;

	@JsonView(ProductComplexView.class)
	private Set<Author> authors;
	
	@JsonView(ProductSimpleView.class)
	private String publisher;
	
	//@JsonView(ProductComplexView.class)
	//private Collection<String> processedConnectors;
	
	@JsonView(ProductComplexView.class)
	private Map<String,Long> dollarPrice = null;
	
	@JsonView(ProductSimpleView.class)
	private Long minPrice = null;	
	
	@JsonView(ProductSimpleView.class)
	private Double mainRating = null;
	
	@JsonView(ProductSimpleView.class)
	private Date lastPriceUpdate = null;
	
	@JsonIgnore
	private User user;

	@JsonView(ProductComplexView.class)
	private Collection<ConnectorInfo> connectorInfo;
	
	@JsonView(ProductComplexView.class)
	private Map<String,String> connectorReferences;
	
	@JsonView(ProductComplexView.class)
	@JsonIgnoreProperties({"product"})
	private SortedSet<Rating> ratings = null;
	
	@JsonView(ProductComplexView.class)
	@JsonInclude(Include.ALWAYS) 
	private SortedMap<String,String> externalLinks = null;
	
	public static Collection<SerializableProduct> cloneProduct(Collection<Product> products){
		return cloneProduct(products,null);
	}
	
	public static Collection<SerializableProduct> cloneProduct(Collection<Product> products, String [] ignoreProperties){
		
		Collection<SerializableProduct> result = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth!=null) {
			user = new User();
			user.setUsername(auth.getName());
		}
		if (products!=null){
			Iterator<Product> iterator = products.iterator();
			if (iterator!=null){
				result = new ArrayList<>();
				while(iterator.hasNext()){
					Product product = iterator.next();			
					result.add(new SerializableProduct(product, user, ignoreProperties));
				}
			}
		}
		return result;
	}
	
	public static SerializableProduct cloneProduct(Product product, Collection<ConnectorInfo> connectorInfo){
		SerializableProduct result = cloneProduct(product);
		result.setConnectorInfo(connectorInfo);
		return result;
	}
	
	public static SerializableProduct cloneProduct(Product product){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth!=null) {
			user = new User();
			user.setUsername(auth.getName());
		}		
		
		return new SerializableProduct(product, user,null);
	}
	
	public SerializableProduct(){}
	
	public SerializableProduct(Product productToClone, User user, String[] ignoreProperties) {
		try {		
			if (ignoreProperties!=null){
				BeanUtils.copyProperties(productToClone, this, ignoreProperties);
			} else {
				BeanUtils.copyProperties(productToClone, this);
			}
		}catch (Exception e){
			logger.error("Exception when copying User to serializable", e);
		}

		this.user = user;
		if (user!=null){
			this.owned = productToClone.getOwners()!=null && productToClone.getOwners().contains(user);
			this.ownedAnotherLanguage = productToClone.getOwnersOtherLanguage()!=null && productToClone.getOwnersOtherLanguage().contains(user);
			this.wished = productToClone.getWishers()!= null && productToClone.getWishers().contains(user);
		}
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public void setCategoryValues(Set<CategoryValue> categoryValues) {
		this.categoryValues = categoryValues;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}
  
	public Boolean getOwned() {
		return owned;
	}

	public void setOwned(Boolean owned) {
		this.owned = owned;
	}

	public Boolean getOwnedAnotherLanguage() {
		return ownedAnotherLanguage;
	}

	public void setOwnedAnotherLanguage(Boolean ownedAnotherLanguage) {
		this.ownedAnotherLanguage = ownedAnotherLanguage;
	}

	public Boolean getWished() {
		return wished;
	}

	public void setWished(Boolean wished) {
		this.wished = wished;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getUniversalReference() {
		return universalReference;
	}

	public void setUniversalReference(String universalReference) {		
		this.universalReference = universalReference;
		if (this.universalReference!=null && ("".equals(this.universalReference.trim()))){
			this.universalReference = null;
		}
	}

	public Set<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<Author> authors) {
		this.authors = authors;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Map<String, Long> getDollarPrice() {
		return dollarPrice;
	}

	public void setDollarPrice(Map<String, Long> dollarPrice) {
		this.dollarPrice = dollarPrice;
	}
	
	public Long getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Long minPrice) {
		this.minPrice = minPrice;
	}

	public Date getLastPriceUpdate() {
		return lastPriceUpdate;
	}

	public void setLastPriceUpdate(Date lastPriceUpdate) {
		this.lastPriceUpdate = lastPriceUpdate;
	}

	public Collection<ConnectorInfo> getConnectorInfo() {
		return connectorInfo;
	}

	public void setConnectorInfo(Collection<ConnectorInfo> connectorInfo) {
		this.connectorInfo = connectorInfo;
	}

	public Map<String, String> getConnectorReferences() {
		return connectorReferences;
	}

	public void setConnectorReferences(Map<String, String> connectorReferences) {
		this.connectorReferences = connectorReferences;
	}

	public Product deserializeProduct(){
		Product product = new Product();
		PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();		
		try {
			propertyUtilsBean.copyProperties(product, this);
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e) {
			logger.error("Exception when deserializing Product into DAO", e);
		}
		return product;
	}
	
	public Double getMainRating() {
		return mainRating;
	}

	public void setMainRating(Double mainRating) {
		this.mainRating = mainRating;
	}

	public SortedSet<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(SortedSet<Rating> ratings) {
		this.ratings = ratings;
	}

	public SortedMap<String, String> getExternalLinks() {
		return externalLinks;
	}

	public void setExternalLinks(SortedMap<String, String> externalLinks) {		
		this.externalLinks = externalLinks;
	}
	

}

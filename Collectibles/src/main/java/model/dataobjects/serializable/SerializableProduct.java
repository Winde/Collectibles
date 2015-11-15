package model.dataobjects.serializable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import model.dataobjects.Author;
import model.dataobjects.Author.AuthorView;
import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.HierarchyNode.HierarchySimpleView;
import model.dataobjects.Image;
import model.dataobjects.Image.ImageSimpleView;
import model.dataobjects.Product;
import model.dataobjects.SimpleIdDao.SimpleIdDaoView;
import model.dataobjects.User;
import model.dataobjects.supporting.ObjectList.ObjectListView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

public class SerializableProduct {

	
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
	private Date releaseDate = null;
			
	@JsonView(ProductSimpleView.class)
	private String universalReference = null;
	
	@JsonView(ProductComplexView.class)
	private String amazonUrl = null;
	
	@JsonView(ProductComplexView.class)
	private String goodreadsUrl = null;
	
	@JsonView(ProductComplexView.class)
	private String drivethrurpgUrl = null;

	
	@JsonView(ProductComplexView.class)
	private Set<Author> authors;
	
	@JsonView(ProductComplexView.class)
	private String goodreadsRelatedLink;

	@JsonView(ProductSimpleView.class)
	private String publisher;
	
	@JsonView(ProductComplexView.class)
	private Collection<String> processedConnectors;
	
	@JsonView(ProductComplexView.class)
	private Map<String,Long> dollarPrice = null;
	
	
	@JsonIgnore
	private User user;
	
	public static Collection<SerializableProduct> cloneProduct(Collection<Product> products){
		
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
					result.add(new SerializableProduct(product, user));
				}
			}
		}
		return result;
	}
	
	public static SerializableProduct cloneProduct(Product product){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth!=null) {
			user = new User();
			user.setUsername(auth.getName());
		}		
		return new SerializableProduct(product, user);
	}
	
	public SerializableProduct(){}
	
	public SerializableProduct(Product productToClone, User user) {
		PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
		try {
			propertyUtilsBean.copyProperties(this, productToClone);			
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.user = user;
		if (user!=null){
			this.owned = productToClone.getOwners()!=null && productToClone.getOwners().contains(user);
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
	}

	public String getAmazonUrl() {
		return amazonUrl;
	}

	public void setAmazonUrl(String amazonUrl) {
		this.amazonUrl = amazonUrl;
	}

	public String getGoodreadsUrl() {
		return goodreadsUrl;
	}

	public void setGoodreadsUrl(String goodreadsUrl) {
		this.goodreadsUrl = goodreadsUrl;
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

	public Collection<String> getProcessedConnectors() {
		return processedConnectors;
	}

	public void setProcessedConnectors(Collection<String> processedConnectors) {
		this.processedConnectors = processedConnectors;
	}

	public String getDrivethrurpgUrl() {
		return drivethrurpgUrl;
	}

	public void setDrivethrurpgUrl(String drivethrurpgUrl) {
		this.drivethrurpgUrl = drivethrurpgUrl;
	}

	public Map<String, Long> getDollarPrice() {
		return dollarPrice;
	}

	public void setDollarPrice(Map<String, Long> dollarPrice) {
		this.dollarPrice = dollarPrice;
	}

	public Product deserializeProduct(){
		Product product = new Product();
		PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();		
		try {
			propertyUtilsBean.copyProperties(product, this);
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return product;
	}


	
}

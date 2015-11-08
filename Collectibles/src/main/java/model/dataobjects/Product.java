package model.dataobjects;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.http.client.HttpResponseException;

import model.connection.amazon.AmazonConnector;
import model.connection.amazon.TooFastConnectionException;
import model.dataobjects.HierarchyNode.HierarchySimpleView;
import model.dataobjects.Image.ImageSimpleView;
import model.dataobjects.SimpleIdDao.SimpleIdDaoView;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class Product extends SimpleIdDao{

	public interface ProductSimpleView  extends SimpleIdDaoView{};
	public interface ProductComplexView extends ProductSimpleView {};
	public interface ProductListView extends ProductSimpleView,HierarchySimpleView,ImageSimpleView{};
	
	@ManyToOne
	@JsonIgnoreProperties({ "father", "children"})
	@JsonView(ProductSimpleView.class)
	private HierarchyNode hierarchyPlacement;
	
	@OneToMany(fetch=FetchType.LAZY)
	@JsonView(ProductComplexView.class)
	private Set<CategoryValue> categoryValues;
	
	@Column(unique=true )
	@JsonView(ProductSimpleView.class)
	private String reference = null;
	
	@Column
	@Lob @Basic
	@JsonView(ProductSimpleView.class)
	private String description = null;

	@Column
	@JsonView(ProductSimpleView.class)
	private String name = null;

	@OneToMany(fetch=FetchType.LAZY,cascade = {CascadeType.REMOVE})
	@JsonIgnoreProperties({ "data" })
	@JsonView(ProductSimpleView.class)
	private List<Image> images = null;

	@Column
	@JsonView(ProductSimpleView.class)
	private Boolean owned = Boolean.FALSE;	
	
	@Column	
	@JsonView(ProductSimpleView.class)
	private Date releaseDate = null;
			
	@Column
	@JsonView(ProductSimpleView.class)
	private String amazonReference = null;
	
	@Column
	private Boolean hasAmazonImage = null;
	
	@Column
	private String amazonUrl = null;
	
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

	public HierarchyNode getHierarchyPlacement() {
		return hierarchyPlacement;
	}

	public void setHierarchyPlacement(HierarchyNode hierarchyPlacement) {
		this.hierarchyPlacement = hierarchyPlacement;
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

	public boolean getOwned() {
		return Boolean.TRUE.equals(this.owned);
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

	
	

	public Boolean getHasAmazonImage() {
		return hasAmazonImage;
	}

	public void setHasAmazonImage(Boolean hasAmazonImage) {
		this.hasAmazonImage = hasAmazonImage;
	}

	public String getAmazonReference() {
		return amazonReference;
	}

	public void setAmazonReference(String amazonReference) {
		this.amazonReference = amazonReference;
	}

	public String getAmazonUrl() {
		return amazonUrl;
	}

	public void setAmazonUrl(String amazonUrl) {
		this.amazonUrl = amazonUrl;
	}
	
	

	public String toString(){
		return "{" + this.getId() + " - " + this.getName() + " - " + this.getDescription() +"}";		
	}
	
}


package model.dataobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

class Rating_PK implements Serializable{
	private Product product = null;
	private String provider = null;
}

@Entity(name="Rating")
@Table(name="product_rating")
@IdClass(Rating_PK.class)
public class Rating implements Comparable<Rating>,Serializable {
		
	@ManyToOne
	@JoinColumn(name="product_id", referencedColumnName="id")
	@Id
	private Product product = null;
	
	@Column(name="rating")
	private Double rating = null;
	
	@Column(name="priority")
	private Long priority = null;
	
	@Column(name="provider")
	@Id
	private String provider = null;
	
	@Column(name="ratings_count")
	private Long ratingsCount = null;
	
	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}
	
	public String getProvider() {		
		return this.provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
	public String toString(){
		return "{" + this.getProvider() + "- "+this.getRating() + "}";
	}

	public Long getRatingsCount() {
		return ratingsCount;
	}

	public void setRatingsCount(Long ratingsCount) {
		this.ratingsCount = ratingsCount;
	}

	@Override
	public int hashCode(){
		Integer hash = null;
		if (this.getPriority()!=null){
			hash = this.getPriority().hashCode();
		} else{
			hash = 0;
		}		
		return hash;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof Rating){
			Rating oR = (Rating) o;
			if (this.getProvider()!=null){
				return this.getProvider().equals(oR.getProvider());
			} else{
				return false;
			}
		} else{
			return false;
		}
	}
	
	@Override
	public int compareTo(Rating oR) {		
		if (this.getPriority()==null){
			return -1;
		} else if ( oR.getPriority()==null ){
			return 1;
		} else {
			int comparison = this.getPriority().compareTo( oR.getPriority());
			if (comparison == 0){
				if (this.equals(oR)){
					return 0;
				} else{
					return -1;
				}
			} else{
				return -1*comparison;
			}
		}		
		
	}


	
}

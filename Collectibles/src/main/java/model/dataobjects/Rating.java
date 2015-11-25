package model.dataobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name="Rating")
@Table(name="product_rating")
public class Rating implements Comparable<Rating>, Serializable {

	@OneToOne
	@Id
	@JoinColumn(name="product_id", referencedColumnName="id")	
	Product product = null;
	
	@Column(name="rating")
	private Double rating = null;
	
	@Column(name="priority")
	private Long priority = null;
	
	@Column(name="provider")
	@Id
	private String provider = null;
	
	
	
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
		return provider;
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
	public int compareTo(Rating o) {
		if (this.getPriority()==null){
			return -1;
		} else if (o.getPriority()==null ){
			return 1;
		} else {
			int comparison = this.getPriority().compareTo(o.getPriority());
			if (comparison == 0){
				if (this.getProvider()!=null && this.getProvider().equals(o.getProvider()) || (this.getProvider()==null && o.getProvider()==null)){
					return 0;
				} else{
					return -1;
				}
			} else{
				return comparison;
			}
		}		
		
	}

	
}

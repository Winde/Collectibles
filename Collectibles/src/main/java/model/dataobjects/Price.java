package model.dataobjects;

import java.io.Serializable;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

class PricePK implements Serializable {
	private Product product = null;	
	private String type = null;
	private String connectorName = null;

}

@Entity(name="Price")
@Table(name="product_price")
@IdClass(PricePK.class)
public class Price implements Serializable, Comparable<Price>{
	
	
	
	@Column(name="connector")
	@Id
	private String connectorName;
	
	@ManyToOne
	@JoinColumn(name="product_id", referencedColumnName="id")
	@Id
	private Product product;
	
	@Column(name="type")
	@Id
	private String type = null;
	
	@Column(name="price")
	private Long price;
	
	@Column(name="link")
	private String link;

	
	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	@Override
	public int hashCode(){
		Integer hash = null;
		if (this.getConnectorName()!=null){
			hash = this.getConnectorName().hashCode();
		} else{
			hash = 0;
		}		
		return hash;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof Price){
			Price oP = (Price) o;			
				return this.getConnectorName()!=null && this.getType()!=null && this.getProduct()!=null &&
						this.getConnectorName().equals(oP.getConnectorName()) &&
						this.getType().equals(oP.getType()) &&
						this.getProduct().equals(oP.getProduct());			
		} 
		return false;
	}
	

	@Override
	public int compareTo(Price o) {
		if (this.getPrice()==null || o.getPrice()==null){
			return -1;
		} else {
			int comparison = this.getPrice().compareTo(o.getPrice());
			if (comparison ==0){
				if (this.equals(o)){
					return 0;
				} else{
					return -1;
				}
			} else {
				return comparison;
			}
		}
	}
	
	
	public String toString(){
		return "{" + this.getProduct() + "; " + this.getConnectorName() + " - " + this.getType() + "; " + this.getPrice() + "}";
	}
}

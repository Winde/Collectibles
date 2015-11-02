package model.dataobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class CategoryValue extends SimpleIdDao{
	
	@Column
	private String name;

	@ManyToOne(fetch=FetchType.LAZY)	
	@JoinColumn(name = "category_id")	
	@JsonIgnoreProperties({"categoryValues","hibernateLazyInitializer", "handler"})
	private Category category;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;		
	}
	
	
}

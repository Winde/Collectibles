package model.dataobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class Author implements Comparable<Author>{

	public interface AuthorView {};

	@Id
	@Column
	@JsonView(AuthorView.class)
	private String id;
	
	@Column(name="name")
	@JsonView(AuthorView.class)
	private String name;
	
	@Column(name="image_url")
	@JsonView(AuthorView.class)
	private String imageUrl;
	
	@Column(name="goodreads_author_link")
	@JsonView(AuthorView.class)
	private String goodreadsAuthorLink;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getGoodreadsAuthorLink() {
		return goodreadsAuthorLink;
	}
	public void setGoodreadsAuthorLink(String goodreadsAuthorLink) {
		this.goodreadsAuthorLink = goodreadsAuthorLink;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof Author) {			
			Author other = (Author) o;			
			if (this.getId()==null || other.getId()==null) {
				return false;
			} else {
				
				return this.getId().equals(other.getId());
			}
		} else {
			return false;		
		}
	}
	

	@Override
	public int hashCode(){
		if (this.getId()!=null) {
			return this.getId().hashCode();
		} else {
			return 0;
		}
	}

	@Override
	public int compareTo(Author other) {
		if (this.getId()==null || other.getId()==null) {
			return -1;
		} else {
			return this.getId().compareTo(other.getId());
		}

	}

	public String toString(){
		return "{ id: "+this.getId() + ", name: " +this.getName() + ", image: " +this.getImageUrl()+ ", url: " +this.getGoodreadsAuthorLink() +" }";
		
	}
}

package model.dataobjects;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name="User")
public class User {

	@Id
	@Column(name="username")
	private String username;
	
	@Column(name="password")
	@JsonIgnore	
	private String password;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="roles", joinColumns=@JoinColumn(name="id"))
	@Column(name="roles")	
	private Collection<String> roles;
	
	public User() {
		roles = new ArrayList<>();
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Collection<String> getRoles() {
		return roles;
	}

	public void setRoles(Collection<String> roles) {
		this.roles = roles;
	}
	

	@Override
	public boolean equals(Object o){
		if (o instanceof User) {			
			User other = (User) o;			
			if (this.getUsername()==null || other.getUsername()==null) {
				return false;
			} else {
				
				return this.getUsername().equals(other.getUsername());
			}
		} else {
			return false;		
		}
	}
	

	@Override
	public int hashCode(){
		if (this.getUsername()!=null) {
			return this.getUsername().hashCode();
		} else {
			return 0;
		}
	}


}

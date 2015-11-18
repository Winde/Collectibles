package model.dataobjects.serializable;

import model.dataobjects.User;

import org.springframework.beans.BeanUtils;

public class SerializableUser {

	private Long id = null;
	private String contactName = null;
	
	public SerializableUser(){}
	
	public SerializableUser(User userToClone){
		this(userToClone,null);
	}
	
	public SerializableUser(User userToClone, String[] ignoreProperties){
		try {		
			if (ignoreProperties!=null){
				BeanUtils.copyProperties(userToClone, this, ignoreProperties);
			}else {
				BeanUtils.copyProperties(userToClone, this);
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	
	
}

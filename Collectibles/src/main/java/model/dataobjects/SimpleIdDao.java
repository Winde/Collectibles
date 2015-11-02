package model.dataobjects;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SimpleIdDao implements Comparable<SimpleIdDao>{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	
	@Override
	public boolean equals(Object o){
		if (o instanceof SimpleIdDao) {
			SimpleIdDao other = (SimpleIdDao) o;
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
	public int compareTo(SimpleIdDao other) {		
		if (this.getId()==null || other.getId()==null) {
			return -1;
		} else {
			return this.getId().compareTo(other.getId());
		}

	}

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}
	
	
}

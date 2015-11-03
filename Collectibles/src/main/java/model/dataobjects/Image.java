package model.dataobjects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity(name="image")
public class Image extends SimpleIdDao{

	
	@Column(name="data")
	@Lob 		
	private byte[] data;
	
	@Column(name="main")
	private boolean main;

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String toString(){
		return "{"+id+"}";
	}
	
}

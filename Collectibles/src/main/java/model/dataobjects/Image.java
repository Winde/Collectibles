package model.dataobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import com.fasterxml.jackson.annotation.JsonView;

@Entity(name="Image")
public class Image extends SimpleIdDao{

	
	public interface ImageSimpleView{};
	
	@Column(name="data")
	@Lob 		
	private byte[] data;
	
	@Column(name="main")
	@JsonView(ImageSimpleView.class)
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

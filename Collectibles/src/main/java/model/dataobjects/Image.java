package model.dataobjects;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
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

	@Column(name="width")
	private Integer width;
	
	@Column(name="height")
	private Integer height;
	
	
	
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
		try {
			InputStream in = new ByteArrayInputStream(data);
			BufferedImage bimg = ImageIO.read(in);
			this.setWidth(bimg.getWidth());
			this.setHeight(bimg.getHeight());			
			in.close();	
		}catch (Exception ex){
			ex.printStackTrace();
		}		
		this.data = data;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer i) {
		this.width = i;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer i) {
		this.height = i;
	}

	public boolean isBigger(Image other) {
		if (this.getWidth()!=null && this.getHeight()!=null && other.getWidth()!=null && other.getHeight()!=null){
			return (this.getWidth() * this.getHeight()) > (other.getWidth() * other.getHeight());
		}else {
			if (this.getData()==null) {
				return false;
			} else if (other.getData()==null){
				return true;
			} else {
				return this.getData().length > other.getData().length;
			}
		}
		
	}
	
	public String toString(){
		return "{"+id+"}";
	}
	
}

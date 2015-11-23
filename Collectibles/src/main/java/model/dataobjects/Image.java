package model.dataobjects;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity(name="Image")
public class Image extends SimpleIdDao{

	private static final Logger logger = LoggerFactory.getLogger(Image.class);	
	
	private static final int thumbSize = 250;
	
	public interface ImageSimpleView{};
	
	@Column(name="data")
	@Lob 
	@Basic(fetch = FetchType.LAZY)
	@JsonIgnore
	private byte[] data;
	
	@Column(name="thumb")
	@Lob
	@JsonIgnore
	private byte[] thumb;
	
	@Column(name="main")
	@JsonView(ImageSimpleView.class)
	private boolean main;

	@Column(name="width")
	private Integer width;
	
	@Column(name="height")
	private Integer height;
	
	@Column(name="not_book")
	@JsonView(ImageSimpleView.class)
	private Boolean notBook;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JsonIgnore
	private Product product;
	
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
			if (this.getThumb()==null){
				this.createThumb(bimg);
			}
			in.close();	
		}catch (Exception e){
			logger.error("Exception when reading image data", e);
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
	
	public byte[] getThumb() {
		if (thumb==null && this.getData()!=null) {
			this.createThumb(this.getData());
		}		
		return thumb;
	}

	public void createThumb(byte[] data) {
		InputStream in = new ByteArrayInputStream(data);
		BufferedImage bimg = null;
		try {
			bimg = ImageIO.read(in);
			createThumb(bimg);
		} catch (IOException e) {
			logger.error("Exception when creating thumb", e);
		}
				
	}
	
	private void createThumb(BufferedImage bimg) throws IOException {
		logger.info("GENERATING THUMBNAIL");
		BufferedImage thumb = Scalr.resize(bimg,  Scalr.Method.ULTRA_QUALITY, thumbSize);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( thumb, "jpg", baos );
		baos.flush();
		byte[] thumbData = baos.toByteArray();
		baos.close();
		this.setThumb(thumbData);
		
	}

	public void setThumb(byte[] thumb) {
		this.thumb = thumb;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	public Boolean getNotBook() {
		return notBook;
	}

	public void setNotBook(Boolean notBook) {
		this.notBook = notBook;
	}

	public String toString(){
		return "{"+id+"}";
	}
	
}

package web.supporting.error.exceptions;

import model.dataobjects.serializable.SerializableProduct;

public class NotEnoughTimeToParseException extends Exception {

	private SerializableProduct serializableProduct = null;

	public NotEnoughTimeToParseException(SerializableProduct serializableProduct){
		this.serializableProduct = serializableProduct;
	}

	public SerializableProduct getProduct() {
		return serializableProduct;
	}

	public void setProduct(SerializableProduct serializableProduct) {
		this.serializableProduct = serializableProduct;
	}
	
	
}

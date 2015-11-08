package model.connection.amazon;

public class AmazonConnector {


	public static String geImageUrl(String id) throws Exception{
		return ItemLookup.getInstance().getImage(id);
	}
	
	public static String getDescription(String id) throws TooFastConnectionException{
		return ItemLookup.getInstance().getDescription(id);
	}

	public static String getAmazonUrl(String id) throws TooFastConnectionException {
		return ItemLookup.getInstance().getAmazonUrl(id);
	}
}

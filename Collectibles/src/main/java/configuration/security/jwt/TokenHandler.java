package configuration.security.jwt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class TokenHandler {

	private static final String HMAC_ALGO = "HmacSHA256";
	private static final String SEPARATOR = ".";
	private static final String SEPARATOR_SPLITTER = "\\."; 
	
	private final Mac hmac;
	
	public TokenHandler(byte[] secretKey) {
		try {
			hmac = Mac.getInstance(HMAC_ALGO);
			hmac.init(new SecretKeySpec(secretKey, HMAC_ALGO));
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new IllegalStateException(
				"failed to initialize HMAC: " + e.getMessage(), e);
		}
	}
	
	private synchronized byte[] createHmac(byte[] content) {
		return hmac.doFinal(content);
	}
	
	private String toBase64(byte [] base64Bytes) {
		return Base64.encodeBase64String(base64Bytes);
	}
	
	
	private byte [] fromBase64(String base64String) {
		return Base64.decodeBase64(base64String);
	}
	

	public String createTokenForUser(UserDetailsImpl user) {
		byte[] userBytes = UserDetailsImpl.toJSON(user);
		byte[] hash = createHmac(userBytes);
		final StringBuilder sb = new StringBuilder(170);
		sb.append(toBase64(userBytes));
		sb.append(SEPARATOR);
		sb.append(toBase64(hash));		
		return sb.toString();
	}


	public UserDetailsImpl parseUserFromToken(String token) {
		
		System.out.println("Parsing User from Token");
		
		final String[] parts = token.split(SEPARATOR_SPLITTER);

		if (parts.length == 2 && parts[0].length() > 0 && parts[1].length() > 0) {
			try {
				final byte[] userBytes = fromBase64(parts[0]);
				final byte[] hash = fromBase64(parts[1]);

				boolean validHash = Arrays.equals(createHmac(userBytes), hash);
				
				
				
				if (validHash) {
					System.out.println("Valid Hash");
					
					String json = new String(userBytes,"UTF-8");
										
					final UserDetailsImpl user = UserDetailsImpl.fromJSON(json);
					
					if (user!=null && new Date().getTime() < user.getExpires()) {
						System.out.println("User is not expired");						
						return user;
					} else {
						System.out.println("User is expired");
					}
				}
			} catch (IllegalArgumentException e) {
				//log tampering attempt here
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
		
	}

}

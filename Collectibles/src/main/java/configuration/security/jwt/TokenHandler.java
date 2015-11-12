package configuration.security.jwt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class TokenHandler {

	private static final String CIPHER_NAME = "AES/ECB/PKCS5Padding";
	private static final String KEY_CIPHER = "AES";
	private static final String HMAC_ALGO = "HmacSHA256";
	private static final String SEPARATOR = ".";
	private static final String SEPARATOR_SPLITTER = "\\."; 
	
	private final Mac hmac;
	private SecretKeySpec encryptKey = null;
	
	public TokenHandler(byte[] secretKey, byte[] encryptKey) {
		try {
			hmac = Mac.getInstance(HMAC_ALGO);
			hmac.init(new SecretKeySpec(secretKey, HMAC_ALGO));
			this.encryptKey =  new SecretKeySpec(encryptKey, KEY_CIPHER);
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
	
	private String cipher(String toProcess, int cryptMode){
		String result = null;
		byte [] resultBytes = null;
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(CIPHER_NAME);
			cipher.init(cryptMode, encryptKey);
			resultBytes = cipher.doFinal(toProcess.getBytes());
			if (resultBytes != null){
				result = new String(resultBytes, "UTF-8");
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private String encrypt(String toBeEncrypted){
		//return cipher(toBeEncrypted,Cipher.ENCRYPT_MODE);
		return toBeEncrypted;
	}

	private String decrypt(String toBeDecrypted){
		//return cipher(toBeDecrypted,Cipher.DECRYPT_MODE);
		return toBeDecrypted;
	}
	
	public String createTokenForUser(UserDetailsImpl user) {
		byte[] userBytes = UserDetailsImpl.toJSON(user);
		byte[] hash = createHmac(userBytes);
		final StringBuilder sb = new StringBuilder(170);
		sb.append(toBase64(userBytes));
		sb.append(SEPARATOR);
		sb.append(toBase64(hash));		
		return this.encrypt(sb.toString());
	}


	public UserDetailsImpl parseUserFromToken(String token) {
		String tokenDecrypted = null;
		if (token!=null){
			tokenDecrypted = this.decrypt(token); 
		}
		if (tokenDecrypted==null){
			return null;
		}
		final String[] parts = tokenDecrypted.split(SEPARATOR_SPLITTER);

		if (parts.length == 2 && parts[0].length() > 0 && parts[1].length() > 0) {
			try {
				final byte[] userBytes = fromBase64(parts[0]);
				final byte[] hash = fromBase64(parts[1]);

				boolean validHash = Arrays.equals(createHmac(userBytes), hash);
				
				
				
				if (validHash) {					
					String json = new String(userBytes,"UTF-8");
										
					final UserDetailsImpl user = UserDetailsImpl.fromJSON(json);
					
					if (user!=null && new Date().getTime() < user.getExpires()) {											
						return user;
					} else {
						System.out.println("User is expired");
					}
				} else{
					System.out.println("Invalid hash received");
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

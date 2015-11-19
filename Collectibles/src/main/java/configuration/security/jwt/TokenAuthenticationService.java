package configuration.security.jwt;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TokenAuthenticationService {

	private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);
		
	private static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
	private static final long TEN_DAYS = 1000 * 60 * 60 * 24 * 10;
	private static final long TWO_HOURS = 1000 * 60 * 60 * 2;
	private static final long THIRTY_MINUTES = 1000 * 60 * 30;
 
	private TokenHandler tokenHandler = null;
 
	@Autowired
	public TokenAuthenticationService(@Value("${token.secret}") String secret, @Value("${token.crypt.key}")String cryptKey) {			
		try {
			tokenHandler = new TokenHandler(DatatypeConverter.parseBase64Binary(secret),cryptKey.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {			
			e.printStackTrace();
		}
	}
 
	public void addAuthentication(HttpServletResponse response, UserAuthentication authentication) {
		final UserDetailsImpl user = authentication.getDetails();
		user.setExpires(System.currentTimeMillis() + THIRTY_MINUTES);
		
		logger.info("Setting authentication for user, expires: " + user.getExpires() );
		String token = tokenHandler.createTokenForUser(user);
		//logger.info("Setting token:" + token);
		response.addHeader(AUTH_HEADER_NAME, token);
	}
 
	public Authentication getAuthentication(HttpServletRequest request) {
		final String token = request.getHeader(AUTH_HEADER_NAME);
		
		//logger.info("Obtained token: " + token);
		
		if (token != null) {
			final UserDetailsImpl user = tokenHandler.parseUserFromToken(token);
			logger.info("Parsed user: " + user);
			if (user != null) {
				logger.info("Parsed user expires: " + user.getExpires());
				
				if (user.getExpires()!=null){
					logger.info("Obtaining authentication for user, expires: " + user.getExpires() );
				}
				return new UserAuthentication(user);
			}
		}
		return null;
	}
}

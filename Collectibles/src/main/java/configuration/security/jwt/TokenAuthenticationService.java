package configuration.security.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TokenAuthenticationService {

	private static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
	private static final long TEN_DAYS = 1000 * 60 * 60 * 24 * 10;
	private static final long TWO_HOURS = 1000 * 60 * 60 * 2;
	private static final long THIRTY_MINUTES = 1000 * 60 * 30;
 
	private final TokenHandler tokenHandler;
 
	@Autowired
	public TokenAuthenticationService(@Value("${token.secret}") String secret) {
		tokenHandler = new TokenHandler(DatatypeConverter.parseBase64Binary(secret));
	}
 
	public void addAuthentication(HttpServletResponse response, UserAuthentication authentication) {
		final UserDetailsImpl user = authentication.getDetails();
		user.setExpires(System.currentTimeMillis() + THIRTY_MINUTES);
		response.addHeader(AUTH_HEADER_NAME, tokenHandler.createTokenForUser(user));
	}
 
	public Authentication getAuthentication(HttpServletRequest request) {
		final String token = request.getHeader(AUTH_HEADER_NAME);
		
		if (token != null) {
			final UserDetailsImpl user = tokenHandler.parseUserFromToken(token);
			if (user != null) {
				return new UserAuthentication(user);
			}
		}
		return null;
	}
}
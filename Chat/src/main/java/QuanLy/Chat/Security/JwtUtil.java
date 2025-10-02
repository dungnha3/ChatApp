package QuanLy.Chat.Security;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.accessTokenExpiration}")
	private long accessTokenExpiration;

	@Value("${app.jwt.refreshTokenExpiration}")
	private long refreshTokenExpiration;

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateAccessToken(String subject, Map<String, Object> claims) {
		return Jwts.builder()
			.setSubject(subject)
			.addClaims(claims)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
			.signWith(getKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public String generateRefreshToken(String subject) {
		return Jwts.builder()
			.setSubject(subject)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
			.signWith(getKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public Claims parse(String token) {
		return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
	}

	public String extractUsername(String token) {
		return parse(token).getSubject();
	}

	public boolean validateToken(String token, String username) {
		try {
			Claims claims = parse(token);
			return username.equals(claims.getSubject()) && !claims.getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}
}



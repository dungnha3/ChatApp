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

	@Value("${app.jwt.secret:ZmFrZV9zZWNyZXRfZm9yX2RlbW8=}")
	private String secretBase64;

	@Value("${app.jwt.accessMillis:900000}")
	private long accessMillis;

	@Value("${app.jwt.refreshMillis:1209600000}")
	private long refreshMillis;

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
	}

	public String generateAccessToken(String subject, Map<String, Object> claims) {
		return Jwts.builder()
			.setSubject(subject)
			.addClaims(claims)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + accessMillis))
			.signWith(getKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public String generateRefreshToken(String subject) {
		return Jwts.builder()
			.setSubject(subject)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + refreshMillis))
			.signWith(getKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public Claims parse(String token) {
		return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
	}
}



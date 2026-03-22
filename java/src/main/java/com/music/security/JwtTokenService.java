package com.music.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenService {

	private final SecretKey secretKey;
	private final long expirationMillis;

	public JwtTokenService(
		@Value("${security.jwt.secret:change-me-very-secret-key-32-bytes-min}") String secret,
		@Value("${security.jwt.expiration:86400000}") long expirationMillis
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMillis = expirationMillis;
	}

	public String generateToken(UserDetails userDetails) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMillis);
		return Jwts.builder()
			.setSubject(userDetails.getUsername())
			.setIssuedAt(now)
			.setExpiration(exp)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	// Allow validation without loading user details (used for frontend users)
	public boolean isTokenValid(String token) {
		return !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		Date expiration = extractAllClaims(token).getExpiration();
		return expiration.before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}
}




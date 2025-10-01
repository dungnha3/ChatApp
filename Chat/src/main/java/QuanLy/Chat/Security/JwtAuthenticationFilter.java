package QuanLy.Chat.Security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	public JwtAuthenticationFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				Claims claims = jwtUtil.parse(token);
				String username = claims.getSubject();
				String role = claims.get("role", String.class);
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						username,
						null,
						Collections.singleton(new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role)))
				);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (Exception ignored) {}
		}
		filterChain.doFilter(request, response);
	}
}



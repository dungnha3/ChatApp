package QuanLy.Chat.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import QuanLy.Chat.DTO.UserDTO;
import QuanLy.Chat.Entity.User;
import QuanLy.Chat.Repository.UserRepository;
import QuanLy.Chat.Security.JwtUtil;
import QuanLy.Chat.DTO.auth.RegisterRequest;
import QuanLy.Chat.DTO.auth.LoginRequest;
import QuanLy.Chat.DTO.auth.AuthResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
	}

	@PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
		try {
            if (userRepository.existsByUsername(req.getUsername())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username đã tồn tại");
			}
            User user = new User();
            user.setUsername(req.getUsername());
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            user.setEmail(req.getEmail());
            user.setPhoneNumber(req.getPhoneNumber());
            user.setRole("USER");
            User saved = userRepository.save(user);
            UserDTO dto = new UserDTO(saved.getUserId(), saved.getUsername(), saved.getEmail(), saved.getPhoneNumber(), saved.getAvatarUrl());
            String access = jwtUtil.generateAccessToken(saved.getUsername(), java.util.Map.of("role", saved.getRole(), "uid", saved.getUserId()));
            String refresh = jwtUtil.generateRefreshToken(saved.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(access, refresh, dto));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
		}
	}

	@PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return userRepository.findByUsername(req.getUsername())
                .map(u -> passwordEncoder.matches(req.getPassword(), u.getPasswordHash()) ?
                        ResponseEntity.ok(new AuthResponse(
                                jwtUtil.generateAccessToken(u.getUsername(), java.util.Map.of("role", u.getRole(), "uid", u.getUserId())),
                                jwtUtil.generateRefreshToken(u.getUsername()),
                                new UserDTO(u.getUserId(), u.getUsername(), u.getEmail(), u.getPhoneNumber(), u.getAvatarUrl())
                        ))
                        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai username hoặc password"))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai username hoặc password"));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		return ResponseEntity.noContent().build();
	}

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody String refreshToken) {
        try {
            var claims = jwtUtil.parse(refreshToken);
            String username = claims.getSubject();
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token không hợp lệ");
            }
            var u = userOpt.get();
            return ResponseEntity.ok(new AuthResponse(
                    jwtUtil.generateAccessToken(u.getUsername(), java.util.Map.of("role", u.getRole(), "uid", u.getUserId())),
                    jwtUtil.generateRefreshToken(u.getUsername()),
                    new UserDTO(u.getUserId(), u.getUsername(), u.getEmail(), u.getPhoneNumber(), u.getAvatarUrl())
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token không hợp lệ");
        }
    }
}



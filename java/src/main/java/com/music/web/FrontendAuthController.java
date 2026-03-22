package com.music.web;

import com.music.domain.FrontendUser;
import com.music.security.JwtTokenService;
import com.music.service.FrontendUserService;
import com.music.web.dto.AuthResponse;
import com.music.web.dto.FrontendRegisterRequest;
import com.music.web.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/frontend/auth")
public class FrontendAuthController {

	private final FrontendUserService userService;
	private final JwtTokenService jwtTokenService;

	public FrontendAuthController(FrontendUserService userService, JwtTokenService jwtTokenService) {
		this.userService = userService;
		this.jwtTokenService = jwtTokenService;
	}

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody FrontendRegisterRequest req) {
		FrontendUser user = userService.register(req.getUsername(), req.getPassword(), req.getNickname(), req.getEmail());
		
		UserDetails userDetails = User.builder()
				.username(user.getUsername())
				.password(user.getPasswordHash())
				.authorities(Collections.emptyList())
				.build();
		
		String token = jwtTokenService.generateToken(userDetails);
		return new AuthResponse(token, user.getUsername());
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest req) {
		FrontendUser user = userService.findByUsername(req.getUsername());
		
		if (!userService.validatePassword(req.getUsername(), req.getPassword())) {
			throw new IllegalArgumentException("用户名或密码错误");
		}
		
		if (!user.isActive()) {
			throw new IllegalArgumentException("账号已被禁用");
		}

		UserDetails userDetails = User.builder()
				.username(user.getUsername())
				.password(user.getPasswordHash())
				.authorities(Collections.emptyList())
				.build();

		String token = jwtTokenService.generateToken(userDetails);
		return new AuthResponse(token, user.getUsername());
	}

	@GetMapping("/me")
	public FrontendUser me(@RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		return userService.findByUsername(username);
	}

	@PutMapping("/profile")
	public FrontendUser updateProfile(@Valid @RequestBody Map<String, String> req, @RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		FrontendUser user = userService.findByUsername(username);
		
		if (req.containsKey("nickname")) {
			user.setNickname(req.get("nickname"));
		}
		if (req.containsKey("email")) {
			user.setEmail(req.get("email"));
		}
		if (req.containsKey("avatarUrl")) {
			user.setAvatarUrl(req.get("avatarUrl"));
		}
		if (req.containsKey("bio")) {
			user.setBio(req.get("bio"));
		}
		
		return userService.update(user.getId(), user);
	}

	@PutMapping("/password")
	public Map<String, String> updatePassword(@RequestBody Map<String, String> req, @RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		
		String oldPassword = req.get("oldPassword");
		String newPassword = req.get("newPassword");
		
		if (!userService.validatePassword(username, oldPassword)) {
			throw new IllegalArgumentException("原密码错误");
		}
		
		userService.updatePassword(username, newPassword);
		return Map.of("message", "密码修改成功");
	}

	@GetMapping("/stats")
	public Map<String, Object> getStats(@RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		FrontendUser user = userService.findByUsername(username);
		return userService.getUserStats(user.getId());
	}
}




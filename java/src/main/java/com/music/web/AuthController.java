package com.music.web;

import com.music.security.JwtTokenService;
import com.music.web.dto.AuthResponse;
import com.music.web.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenService jwtTokenService;

	public AuthController(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenService = jwtTokenService;
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
			);
			UserDetails userDetails = (UserDetails) auth.getPrincipal();
			String token = jwtTokenService.generateToken(userDetails);
			return new AuthResponse(token, userDetails.getUsername());
		} catch (BadCredentialsException ex) {
			throw ex;
		}
	}

	@GetMapping("/me")
	public UserDetails me(@AuthenticationPrincipal UserDetails userDetails) {
		return userDetails;
	}
}




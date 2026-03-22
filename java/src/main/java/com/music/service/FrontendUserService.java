package com.music.service;

import com.music.domain.FrontendUser;
import com.music.exception.NotFoundException;
import com.music.repository.FrontendUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class FrontendUserService {

	private final FrontendUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final FavoriteService favoriteService;
	private final PlayHistoryService playHistoryService;

	public FrontendUserService(FrontendUserRepository userRepository, PasswordEncoder passwordEncoder, 
	                           FavoriteService favoriteService, PlayHistoryService playHistoryService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.favoriteService = favoriteService;
		this.playHistoryService = playHistoryService;
	}

	public FrontendUser register(String username, String password, String nickname, String email) {
		if (userRepository.existsByUsername(username)) {
			throw new IllegalArgumentException("用户名已存在");
		}
		if (email != null && userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("邮箱已被使用");
		}

		FrontendUser user = new FrontendUser();
		user.setUsername(username);
		user.setPasswordHash(passwordEncoder.encode(password));
		user.setNickname(nickname != null ? nickname : username);
		user.setEmail(email);
		user.setActive(true);
		
		return userRepository.save(user);
	}

	public FrontendUser findByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new NotFoundException("用户不存在"));
	}

	public FrontendUser getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("用户不存在"));
	}

	public FrontendUser update(Long id, FrontendUser update) {
		FrontendUser user = getById(id);
		if (update.getNickname() != null) {
			user.setNickname(update.getNickname());
		}
		if (update.getEmail() != null) {
			user.setEmail(update.getEmail());
		}
		if (update.getAvatarUrl() != null) {
			user.setAvatarUrl(update.getAvatarUrl());
		}
		if (update.getBio() != null) {
			user.setBio(update.getBio());
		}
		return userRepository.save(user);
	}

	public boolean validatePassword(String username, String rawPassword) {
		FrontendUser user = findByUsername(username);
		return passwordEncoder.matches(rawPassword, user.getPasswordHash());
	}

	public void updatePassword(String username, String newPassword) {
		FrontendUser user = findByUsername(username);
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	public java.util.Map<String, Object> getUserStats(Long userId) {
		long favoriteCount = favoriteService.countUserFavorites(userId);
		long playCount = playHistoryService.countUserPlays(userId);
		
		java.util.Map<String, Object> stats = new java.util.HashMap<>();
		stats.put("favoriteCount", favoriteCount);
		stats.put("playCount", playCount);
		return stats;
	}
}




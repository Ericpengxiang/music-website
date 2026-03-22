package com.music.service;

import com.music.domain.AppUser;
import com.music.exception.NotFoundException;
import com.music.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

	private final AppUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<AppUser> listAll() { return userRepository.findAll(); }

	public AppUser getById(Long id) { return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found")); }

	public AppUser create(AppUser user, String rawPassword) {
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		return userRepository.save(user);
	}

	public AppUser update(Long id, AppUser update, String rawPassword) {
		AppUser u = getById(id);
		u.setDisplayName(update.getDisplayName());
		u.setActive(update.isActive());
		if (rawPassword != null && !rawPassword.isEmpty()) {
			u.setPasswordHash(passwordEncoder.encode(rawPassword));
		}
		return userRepository.save(u);
	}

	public void delete(Long id) { userRepository.delete(getById(id)); }

	public Page<AppUser> page(String keyword, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
		if (keyword == null || keyword.isBlank()) {
			return userRepository.findAll(pr);
		}
		return userRepository.findByUsernameContainingIgnoreCase(keyword.trim(), pr);
	}
}




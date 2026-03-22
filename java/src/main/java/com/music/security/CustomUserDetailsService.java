package com.music.security;

import com.music.domain.AppUser;
import com.music.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final AppUserRepository appUserRepository;

	public CustomUserDetailsService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findWithRolesByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return new User(
				user.getUsername(),
				user.getPasswordHash(),
				user.isActive(),
				true,
				true,
				true,
				user.getRoles().stream()
						.map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
						.collect(Collectors.toSet())
		);
	}
}




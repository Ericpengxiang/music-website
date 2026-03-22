package com.music.bootstrap;

import com.music.domain.AppUser;
import com.music.domain.Role;
import com.music.repository.AppUserRepository;
import com.music.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class AdminInitializer implements ApplicationRunner {

	private final RoleRepository roleRepository;
	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminInitializer(RoleRepository roleRepository, AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
		this.roleRepository = roleRepository;
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
			Role r = new Role();
			r.setName("ADMIN");
			return roleRepository.save(r);
		});

		AppUser admin = appUserRepository.findByUsername("admin").orElseGet(() -> {
			AppUser u = new AppUser();
			u.setUsername("admin");
			u.setDisplayName("Administrator");
			return u;
		});
		admin.setActive(true);
		admin.setPasswordHash(passwordEncoder.encode("admin123"));
		admin.setRoles(Collections.singleton(adminRole));
		appUserRepository.save(admin);
	}
}




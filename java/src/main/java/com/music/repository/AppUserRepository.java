package com.music.repository;

import com.music.domain.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByUsername(String username);

	@EntityGraph(attributePaths = "roles")
	Optional<AppUser> findWithRolesByUsername(String username);

	boolean existsByUsername(String username);

	Page<AppUser> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}




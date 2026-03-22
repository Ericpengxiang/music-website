package com.music.repository;

import com.music.domain.FrontendUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FrontendUserRepository extends JpaRepository<FrontendUser, Long> {
	Optional<FrontendUser> findByUsername(String username);
	Optional<FrontendUser> findByEmail(String email);
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
}



package com.music.repository;

import com.music.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
	List<Playlist> findByUserIdOrderByCreatedAtDesc(Long userId);
	List<Playlist> findByIsPublicTrueOrderByPlayCountDesc();
}




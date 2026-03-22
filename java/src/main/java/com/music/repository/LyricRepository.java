package com.music.repository;

import com.music.domain.Lyric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LyricRepository extends JpaRepository<Lyric, Long> {
	Optional<Lyric> findBySongId(Long songId);
	boolean existsBySongId(Long songId);
	void deleteBySongId(Long songId);
}






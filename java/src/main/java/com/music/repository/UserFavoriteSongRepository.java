package com.music.repository;

import com.music.domain.UserFavoriteSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteSongRepository extends JpaRepository<UserFavoriteSong, Long> {
	Optional<UserFavoriteSong> findByUserIdAndSongId(Long userId, Long songId);
	List<UserFavoriteSong> findByUserIdOrderByCreatedAtDesc(Long userId);
	boolean existsByUserIdAndSongId(Long userId, Long songId);
	void deleteByUserIdAndSongId(Long userId, Long songId);
	
	@Query("SELECT f.songId FROM UserFavoriteSong f WHERE f.userId = :userId")
	List<Long> findSongIdsByUserId(Long userId);
	
	long countByUserId(Long userId);
	
	@Query("SELECT f.songId, COUNT(f.id) FROM UserFavoriteSong f GROUP BY f.songId")
	List<Object[]> countBySongIdGroupBy();
}



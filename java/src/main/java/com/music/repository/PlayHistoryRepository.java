package com.music.repository;

import com.music.domain.PlayHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
	Page<PlayHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
	long countByUserId(Long userId);
	
	@Query("SELECT h.songId, COUNT(h.id) FROM PlayHistory h GROUP BY h.songId")
	List<Object[]> countBySongIdGroupBy();
}




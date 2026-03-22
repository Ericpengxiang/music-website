package com.music.repository;

import com.music.domain.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
	Optional<StoryLike> findByUserIdAndStoryId(Long userId, Long storyId);
	boolean existsByUserIdAndStoryId(Long userId, Long storyId);
	void deleteByUserIdAndStoryId(Long userId, Long storyId);
	
	@Query("SELECT sl.storyId FROM StoryLike sl WHERE sl.userId = :userId")
	List<Long> findStoryIdsByUserId(Long userId);
}






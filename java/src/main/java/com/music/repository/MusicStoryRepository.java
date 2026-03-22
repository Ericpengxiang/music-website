package com.music.repository;

import com.music.domain.MusicStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MusicStoryRepository extends JpaRepository<MusicStory, Long> {
	// 获取歌曲的故事（分页，精选+热门优先）
	Page<MusicStory> findBySongIdOrderByFeaturedDescLikeCountDescCreatedAtDesc(Long songId, Pageable pageable);
	
	// 获取精选故事
	Page<MusicStory> findByFeaturedTrueOrderByLikeCountDescCreatedAtDesc(Pageable pageable);
	
	// 按情感标签获取故事
	Page<MusicStory> findByEmotionOrderByLikeCountDescCreatedAtDesc(String emotion, Pageable pageable);
	
	// 获取所有故事（分页，按时间倒序）- 管理员
	Page<MusicStory> findAllByOrderByCreatedAtDesc(Pageable pageable);
	
	// 获取用户的故事
	List<MusicStory> findByUserIdOrderByCreatedAtDesc(Long userId);
	
	// 统计歌曲的故事数
	long countBySongId(Long songId);
	
	// 增加点赞数
	@Modifying
	@Query("UPDATE MusicStory s SET s.likeCount = s.likeCount + 1 WHERE s.id = :storyId")
	void incrementLikeCount(Long storyId);
	
	// 减少点赞数
	@Modifying
	@Query("UPDATE MusicStory s SET s.likeCount = s.likeCount - 1 WHERE s.id = :storyId AND s.likeCount > 0")
	void decrementLikeCount(Long storyId);
}




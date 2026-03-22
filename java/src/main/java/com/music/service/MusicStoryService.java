package com.music.service;

import com.music.domain.MusicStory;
import com.music.domain.StoryLike;
import com.music.repository.FrontendUserRepository;
import com.music.repository.MusicStoryRepository;
import com.music.repository.StoryLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MusicStoryService {

	private final MusicStoryRepository storyRepository;
	private final StoryLikeRepository storyLikeRepository;
	private final FrontendUserRepository userRepository;

	public MusicStoryService(MusicStoryRepository storyRepository, 
	                         StoryLikeRepository storyLikeRepository,
	                         FrontendUserRepository userRepository) {
		this.storyRepository = storyRepository;
		this.storyLikeRepository = storyLikeRepository;
		this.userRepository = userRepository;
	}

	// 发布故事
	@Transactional
	public MusicStory create(Long userId, Long songId, String story, String emotion) {
		MusicStory musicStory = new MusicStory();
		musicStory.setUserId(userId);
		musicStory.setSongId(songId);
		musicStory.setStory(story);
		musicStory.setEmotion(emotion);
		return storyRepository.save(musicStory);
	}

	// 获取歌曲的故事（分页）
	public Page<MusicStory> getStoriesBySong(Long songId, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
		Page<MusicStory> stories = storyRepository.findBySongIdOrderByFeaturedDescLikeCountDescCreatedAtDesc(songId, pr);
		fillUserInfoBatch(stories.getContent());
		return stories;
	}

	// 获取精选故事
	public Page<MusicStory> getFeaturedStories(int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
		Page<MusicStory> stories = storyRepository.findByFeaturedTrueOrderByLikeCountDescCreatedAtDesc(pr);
		fillUserInfoBatch(stories.getContent());
		return stories;
	}

	// 按情感标签获取故事
	public Page<MusicStory> getStoriesByEmotion(String emotion, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
		Page<MusicStory> stories = storyRepository.findByEmotionOrderByLikeCountDescCreatedAtDesc(emotion, pr);
		fillUserInfoBatch(stories.getContent());
		return stories;
	}

	// 批量填充用户信息
	private void fillUserInfoBatch(List<MusicStory> stories) {
		if (stories.isEmpty()) return;
		
		Set<Long> userIds = stories.stream()
				.map(MusicStory::getUserId)
				.collect(java.util.stream.Collectors.toSet());
		
		Map<Long, com.music.domain.FrontendUser> userMap = userRepository.findAllById(userIds).stream()
				.collect(java.util.stream.Collectors.toMap(
						com.music.domain.FrontendUser::getId,
						user -> user
				));
		
		stories.forEach(story -> {
			com.music.domain.FrontendUser user = userMap.get(story.getUserId());
			if (user != null) {
				story.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
				story.setUserAvatar(user.getAvatarUrl());
			}
		});
	}

	// 删除故事
	@Transactional
	public void delete(Long userId, Long storyId) {
		MusicStory story = storyRepository.findById(storyId)
				.orElseThrow(() -> new IllegalArgumentException("故事不存在"));
		if (!story.getUserId().equals(userId)) {
			throw new IllegalArgumentException("无权删除");
		}
		storyRepository.delete(story);
	}

	// 点赞故事
	@Transactional
	public void like(Long userId, Long storyId) {
		if (storyLikeRepository.existsByUserIdAndStoryId(userId, storyId)) {
			return;
		}
		StoryLike like = new StoryLike();
		like.setUserId(userId);
		like.setStoryId(storyId);
		storyLikeRepository.save(like);
		storyRepository.incrementLikeCount(storyId);
	}

	// 取消点赞
	@Transactional
	public void unlike(Long userId, Long storyId) {
		storyLikeRepository.deleteByUserIdAndStoryId(userId, storyId);
		storyRepository.decrementLikeCount(storyId);
	}

	// 检查点赞状态
	public List<Long> getLikedStoryIds(Long userId) {
		return storyLikeRepository.findStoryIdsByUserId(userId);
	}

	// 统计故事数
	public long countBySongId(Long songId) {
		return storyRepository.countBySongId(songId);
	}

	// ========== 管理员功能 ==========
	
	// 获取所有故事（分页）- 管理员
	public Page<MusicStory> getAllStories(int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
		Page<MusicStory> stories = storyRepository.findAllByOrderByCreatedAtDesc(pr);
		fillUserInfoBatch(stories.getContent());
		return stories;
	}

	// 设置精选 - 管理员
	@Transactional
	public MusicStory toggleFeatured(Long storyId) {
		MusicStory story = storyRepository.findById(storyId)
				.orElseThrow(() -> new IllegalArgumentException("故事不存在"));
		story.setFeatured(!story.isFeatured());
		return storyRepository.save(story);
	}

	// 删除故事 - 管理员
	@Transactional
	public void adminDelete(Long storyId) {
		storyRepository.deleteById(storyId);
	}

	// 根据ID获取故事
	public MusicStory getById(Long storyId) {
		MusicStory story = storyRepository.findById(storyId)
				.orElseThrow(() -> new IllegalArgumentException("故事不存在"));
		fillUserInfoBatch(List.of(story));
		return story;
	}
}




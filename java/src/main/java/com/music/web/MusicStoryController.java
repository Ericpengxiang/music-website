package com.music.web;

import com.music.domain.MusicStory;
import com.music.security.JwtTokenService;
import com.music.service.FrontendUserService;
import com.music.service.MusicStoryService;
import com.music.web.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stories")
public class MusicStoryController {

	private final MusicStoryService storyService;
	private final FrontendUserService userService;
	private final JwtTokenService jwtTokenService;

	public MusicStoryController(MusicStoryService storyService, FrontendUserService userService, JwtTokenService jwtTokenService) {
		this.storyService = storyService;
		this.userService = userService;
		this.jwtTokenService = jwtTokenService;
	}

	// 获取歌曲的故事列表
	@GetMapping("/song/{songId}")
	public PageResponse<MusicStory> getBySong(@PathVariable Long songId,
	                                          @RequestParam(name = "page", defaultValue = "0") int page,
	                                          @RequestParam(name = "size", defaultValue = "10") int size) {
		Page<MusicStory> p = storyService.getStoriesBySong(songId, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	// 获取精选故事
	@GetMapping("/featured")
	public PageResponse<MusicStory> getFeatured(@RequestParam(name = "page", defaultValue = "0") int page,
	                                            @RequestParam(name = "size", defaultValue = "20") int size) {
		Page<MusicStory> p = storyService.getFeaturedStories(page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	// 按情感标签获取故事
	@GetMapping("/emotion/{emotion}")
	public PageResponse<MusicStory> getByEmotion(@PathVariable String emotion,
	                                             @RequestParam(name = "page", defaultValue = "0") int page,
	                                             @RequestParam(name = "size", defaultValue = "20") int size) {
		Page<MusicStory> p = storyService.getStoriesByEmotion(emotion, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	// 发布故事
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MusicStory create(@RequestBody Map<String, Object> req, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		Long songId = Long.valueOf(req.get("songId").toString());
		String story = (String) req.get("story");
		String emotion = (String) req.get("emotion");
		
		return storyService.create(userId, songId, story, emotion);
	}

	// 删除故事
	@DeleteMapping("/{storyId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long storyId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		storyService.delete(userId, storyId);
	}

	// 点赞故事
	@PostMapping("/{storyId}/like")
	@ResponseStatus(HttpStatus.CREATED)
	public void like(@PathVariable Long storyId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		storyService.like(userId, storyId);
	}

	// 取消点赞
	@DeleteMapping("/{storyId}/like")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void unlike(@PathVariable Long storyId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		storyService.unlike(userId, storyId);
	}

	// 检查点赞状态
	@GetMapping("/likes/check")
	public Map<String, List<Long>> checkLikes(@RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		List<Long> likedIds = storyService.getLikedStoryIds(userId);
		return Map.of("likedStoryIds", likedIds);
	}

	// 统计故事数
	@GetMapping("/count/song/{songId}")
	public Map<String, Long> count(@PathVariable Long songId) {
		long count = storyService.countBySongId(songId);
		return Map.of("count", count);
	}

	// ========== 管理员接口 ==========
	
	// 获取所有故事（分页）- 管理员
	@GetMapping("/admin/all")
	public PageResponse<MusicStory> getAll(@RequestParam(name = "page", defaultValue = "0") int page,
	                                       @RequestParam(name = "size", defaultValue = "20") int size) {
		Page<MusicStory> p = storyService.getAllStories(page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	// 设置/取消精选 - 管理员
	@PutMapping("/admin/{storyId}/featured")
	public MusicStory toggleFeatured(@PathVariable Long storyId) {
		return storyService.toggleFeatured(storyId);
	}

	// 删除故事 - 管理员
	@DeleteMapping("/admin/{storyId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void adminDelete(@PathVariable Long storyId) {
		storyService.adminDelete(storyId);
	}

	// 获取单个故事详情
	@GetMapping("/{storyId}")
	public MusicStory getById(@PathVariable Long storyId) {
		return storyService.getById(storyId);
	}

	private Long getUserIdFromToken(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		return userService.findByUsername(username).getId();
	}
}




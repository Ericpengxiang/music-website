package com.music.web;

import com.music.domain.Song;
import com.music.security.JwtTokenService;
import com.music.service.FrontendUserService;
import com.music.service.RecommendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

	private final RecommendService recommendService;
	private final FrontendUserService userService;
	private final JwtTokenService jwtTokenService;

	public RecommendController(RecommendService recommendService, FrontendUserService userService, JwtTokenService jwtTokenService) {
		this.recommendService = recommendService;
		this.userService = userService;
		this.jwtTokenService = jwtTokenService;
	}

	// 为用户推荐歌曲
	@GetMapping("/for-you")
	public List<Song> forYou(@RequestParam(name = "limit", defaultValue = "20") int limit,
	                         @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		return recommendService.recommendForUser(userId, limit);
	}

	// 获取相似歌曲
	@GetMapping("/similar/{songId}")
	public List<Song> similar(@PathVariable Long songId,
	                          @RequestParam(name = "limit", defaultValue = "10") int limit) {
		return recommendService.getSimilarSongs(songId, limit);
	}

	private Long getUserIdFromToken(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		return userService.findByUsername(username).getId();
	}
}






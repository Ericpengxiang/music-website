package com.music.web;

import com.music.domain.UserFavoriteSong;
import com.music.security.JwtTokenService;
import com.music.service.FavoriteService;
import com.music.service.FrontendUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

	private final FavoriteService favoriteService;
	private final FrontendUserService userService;
	private final JwtTokenService jwtTokenService;

	public FavoriteController(FavoriteService favoriteService, FrontendUserService userService, JwtTokenService jwtTokenService) {
		this.favoriteService = favoriteService;
		this.userService = userService;
		this.jwtTokenService = jwtTokenService;
	}

	// 收藏歌曲
	@PostMapping("/songs/{songId}")
	@ResponseStatus(HttpStatus.CREATED)
	public void favoriteSong(@PathVariable Long songId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		favoriteService.favoriteSong(userId, songId);
	}

	// 取消收藏歌曲
	@DeleteMapping("/songs/{songId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void unfavoriteSong(@PathVariable Long songId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		favoriteService.unfavoriteSong(userId, songId);
	}

	// 获取用户收藏的歌曲列表
	@GetMapping("/songs")
	public List<UserFavoriteSong> getFavoriteSongs(@RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		return favoriteService.getUserFavoriteSongs(userId);
	}

	// 检查歌曲是否已收藏
	@GetMapping("/songs/{songId}/check")
	public Map<String, Boolean> checkSongFavorited(@PathVariable Long songId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		boolean favorited = favoriteService.isSongFavorited(userId, songId);
		return Map.of("favorited", favorited);
	}

	// 批量检查歌曲是否已收藏
	@GetMapping("/songs/check")
	public Map<String, List<Long>> checkSongsFavorited(@RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		List<Long> favoritedIds = favoriteService.getUserFavoriteSongIds(userId);
		return Map.of("favoritedIds", favoritedIds);
	}

	private Long getUserIdFromToken(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		return userService.findByUsername(username).getId();
	}
}




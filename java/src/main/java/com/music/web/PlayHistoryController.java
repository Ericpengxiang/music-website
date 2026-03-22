package com.music.web;

import com.music.domain.PlayHistory;
import com.music.security.JwtTokenService;
import com.music.service.FrontendUserService;
import com.music.service.PlayHistoryService;
import com.music.web.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class PlayHistoryController {

	private final PlayHistoryService historyService;
	private final FrontendUserService userService;
	private final JwtTokenService jwtTokenService;

	public PlayHistoryController(PlayHistoryService historyService, FrontendUserService userService, JwtTokenService jwtTokenService) {
		this.historyService = historyService;
		this.userService = userService;
		this.jwtTokenService = jwtTokenService;
	}

	// 记录播放
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void recordPlay(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		Long songId = Long.valueOf(request.get("songId").toString());
		Integer duration = request.get("duration") != null ? Integer.valueOf(request.get("duration").toString()) : null;
		historyService.recordPlay(userId, songId, duration);
	}

	// 获取播放历史
	@GetMapping
	public PageResponse<PlayHistory> getHistory(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		Page<PlayHistory> p = historyService.getUserHistory(userId, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	private Long getUserIdFromToken(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		return userService.findByUsername(username).getId();
	}
}




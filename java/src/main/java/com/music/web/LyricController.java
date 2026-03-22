package com.music.web;

import com.music.domain.Lyric;
import com.music.service.LyricService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lyrics")
public class LyricController {

	private final LyricService lyricService;

	public LyricController(LyricService lyricService) {
		this.lyricService = lyricService;
	}

	// 获取歌词（公开）
	@GetMapping("/song/{songId}")
	public ResponseEntity<Lyric> getBySongId(@PathVariable Long songId) {
		return lyricService.getBySongId(songId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// 上传/更新歌词（管理员）
	@PostMapping("/song/{songId}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Lyric createOrUpdate(@PathVariable Long songId, @RequestBody Map<String, String> req) {
		String content = req.get("content");
		String translated = req.get("translated");
		return lyricService.createOrUpdate(songId, content, translated);
	}

	// 删除歌词（管理员）
	@DeleteMapping("/song/{songId}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long songId) {
		lyricService.deleteBySongId(songId);
	}

	// 获取所有歌词（管理员）
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public java.util.List<Lyric> getAllLyrics() {
		return lyricService.getAllLyrics();
	}
}




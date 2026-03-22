package com.music.web;

import com.music.domain.Song;
import com.music.service.RankingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

	private final RankingService rankingService;

	public RankingController(RankingService rankingService) {
		this.rankingService = rankingService;
	}

	// 热门榜
	@GetMapping("/hot")
	public List<Song> hot(@RequestParam(name = "limit", defaultValue = "50") int limit) {
		return rankingService.getHotSongs(limit);
	}

	// 新歌榜
	@GetMapping("/new")
	public List<Song> newSongs(@RequestParam(name = "limit", defaultValue = "50") int limit) {
		return rankingService.getNewSongs(limit);
	}

	// 收藏榜
	@GetMapping("/favorite")
	public List<Song> favorite(@RequestParam(name = "limit", defaultValue = "50") int limit) {
		return rankingService.getFavoriteSongs(limit);
	}
}






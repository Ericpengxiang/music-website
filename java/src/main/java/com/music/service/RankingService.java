package com.music.service;

import com.music.domain.Song;
import com.music.repository.PlayHistoryRepository;
import com.music.repository.SongRepository;
import com.music.repository.UserFavoriteSongRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingService {

	private final SongRepository songRepository;
	private final PlayHistoryRepository playHistoryRepository;
	private final UserFavoriteSongRepository favoriteSongRepository;

	public RankingService(SongRepository songRepository,
	                      PlayHistoryRepository playHistoryRepository,
	                      UserFavoriteSongRepository favoriteSongRepository) {
		this.songRepository = songRepository;
		this.playHistoryRepository = playHistoryRepository;
		this.favoriteSongRepository = favoriteSongRepository;
	}

	// 热门榜（按播放次数）
	public List<Song> getHotSongs(int limit) {
		// 统计每首歌的播放次数
		List<Object[]> playCounts = playHistoryRepository.countBySongIdGroupBy();
		
		if (playCounts.isEmpty()) {
			// 如果没有播放记录，返回最新歌曲
			return getNewSongs(limit);
		}
		
		// 按播放次数排序
		List<Long> topSongIds = playCounts.stream()
				.sorted((a, b) -> Long.compare((Long) b[1], (Long) a[1]))
				.limit(limit)
				.map(arr -> (Long) arr[0])
				.collect(Collectors.toList());
		
		// 获取歌曲详情（保持排序）
		Map<Long, Song> songMap = songRepository.findAllById(topSongIds).stream()
				.collect(Collectors.toMap(Song::getId, s -> s));
		
		return topSongIds.stream()
				.map(songMap::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	// 新歌榜（按创建时间倒序）
	public List<Song> getNewSongs(int limit) {
		PageRequest pr = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
		return songRepository.findAll(pr).getContent();
	}

	// 收藏榜（按收藏次数）
	public List<Song> getFavoriteSongs(int limit) {
		List<Object[]> favCounts = favoriteSongRepository.countBySongIdGroupBy();
		
		if (favCounts.isEmpty()) {
			return getNewSongs(limit);
		}
		
		List<Long> topSongIds = favCounts.stream()
				.sorted((a, b) -> Long.compare((Long) b[1], (Long) a[1]))
				.limit(limit)
				.map(arr -> (Long) arr[0])
				.collect(Collectors.toList());
		
		Map<Long, Song> songMap = songRepository.findAllById(topSongIds).stream()
				.collect(Collectors.toMap(Song::getId, s -> s));
		
		return topSongIds.stream()
				.map(songMap::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}






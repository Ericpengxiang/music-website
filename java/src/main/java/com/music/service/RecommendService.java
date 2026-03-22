package com.music.service;

import com.music.domain.Song;
import com.music.repository.PlayHistoryRepository;
import com.music.repository.SongRepository;
import com.music.repository.UserFavoriteSongRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {

	private final UserFavoriteSongRepository favoriteSongRepository;
	private final PlayHistoryRepository playHistoryRepository;
	private final SongRepository songRepository;

	public RecommendService(UserFavoriteSongRepository favoriteSongRepository,
	                        PlayHistoryRepository playHistoryRepository,
	                        SongRepository songRepository) {
		this.favoriteSongRepository = favoriteSongRepository;
		this.playHistoryRepository = playHistoryRepository;
		this.songRepository = songRepository;
	}

	// 为用户推荐歌曲（基于收藏和播放历史）
	public List<Song> recommendForUser(Long userId, int limit) {
		// 获取用户收藏和播放过的歌曲
		Set<Long> userSongIds = new HashSet<>();
		userSongIds.addAll(favoriteSongRepository.findSongIdsByUserId(userId));
		
		// 获取用户播放历史的歌曲ID
		PageRequest pr = PageRequest.of(0, 50);
		playHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pr)
				.getContent()
				.forEach(h -> userSongIds.add(h.getSongId()));

		if (userSongIds.isEmpty()) {
			// 新用户：返回随机热门歌曲
			return getPopularSongs(limit);
		}

		// 获取这些歌曲的详细信息
		List<Song> userSongs = songRepository.findAllById(userSongIds);
		
		// 提取流派和歌手
		Set<Long> genreIds = userSongs.stream()
				.filter(s -> s.getGenre() != null)
				.map(s -> s.getGenre().getId())
				.collect(Collectors.toSet());
		
		Set<Long> artistIds = userSongs.stream()
				.filter(s -> s.getArtist() != null)
				.map(s -> s.getArtist().getId())
				.collect(Collectors.toSet());

		// 推荐相似流派或相同歌手的歌曲
		List<Song> allSongs = songRepository.findAll();
		List<Song> recommendations = allSongs.stream()
				.filter(s -> !userSongIds.contains(s.getId())) // 排除已收藏/播放
				.filter(s -> {
					boolean sameGenre = s.getGenre() != null && genreIds.contains(s.getGenre().getId());
					boolean sameArtist = s.getArtist() != null && artistIds.contains(s.getArtist().getId());
					return sameGenre || sameArtist;
				})
				.limit(limit)
				.collect(Collectors.toList());

		// 如果推荐不足，补充热门歌曲
		if (recommendations.size() < limit) {
			List<Song> popular = getPopularSongs(limit - recommendations.size());
			recommendations.addAll(popular.stream()
					.filter(s -> !userSongIds.contains(s.getId()))
					.filter(s -> recommendations.stream().noneMatch(r -> r.getId().equals(s.getId())))
					.collect(Collectors.toList()));
		}

		return recommendations;
	}

	// 获取热门歌曲（按ID倒序，简化实现）
	private List<Song> getPopularSongs(int limit) {
		PageRequest pr = PageRequest.of(0, limit);
		return songRepository.findAll(pr).getContent();
	}

	// 获取相似歌曲（基于流派和歌手）
	public List<Song> getSimilarSongs(Long songId, int limit) {
		Song song = songRepository.findById(songId).orElse(null);
		if (song == null) return Collections.emptyList();

		List<Song> allSongs = songRepository.findAll();
		return allSongs.stream()
				.filter(s -> !s.getId().equals(songId))
				.filter(s -> {
					boolean sameGenre = song.getGenre() != null && s.getGenre() != null 
							&& s.getGenre().getId().equals(song.getGenre().getId());
					boolean sameArtist = song.getArtist() != null && s.getArtist() != null 
							&& s.getArtist().getId().equals(song.getArtist().getId());
					return sameGenre || sameArtist;
				})
				.limit(limit)
				.collect(Collectors.toList());
	}
}






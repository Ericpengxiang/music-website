package com.music.service;

import com.music.domain.UserFavoriteSong;
import com.music.repository.UserFavoriteSongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

	private final UserFavoriteSongRepository favoriteSongRepository;

	public FavoriteService(UserFavoriteSongRepository favoriteSongRepository) {
		this.favoriteSongRepository = favoriteSongRepository;
	}

	// 收藏歌曲
	@Transactional
	public void favoriteSong(Long userId, Long songId) {
		if (favoriteSongRepository.existsByUserIdAndSongId(userId, songId)) {
			return; // 已收藏，忽略
		}
		UserFavoriteSong favorite = new UserFavoriteSong();
		favorite.setUserId(userId);
		favorite.setSongId(songId);
		favoriteSongRepository.save(favorite);
	}

	// 取消收藏歌曲
	@Transactional
	public void unfavoriteSong(Long userId, Long songId) {
		favoriteSongRepository.deleteByUserIdAndSongId(userId, songId);
	}

	// 检查是否已收藏
	public boolean isSongFavorited(Long userId, Long songId) {
		return favoriteSongRepository.existsByUserIdAndSongId(userId, songId);
	}

	// 获取用户收藏的所有歌曲
	public List<UserFavoriteSong> getUserFavoriteSongs(Long userId) {
		return favoriteSongRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	// 获取用户收藏的歌曲ID列表
	public List<Long> getUserFavoriteSongIds(Long userId) {
		return favoriteSongRepository.findSongIdsByUserId(userId);
	}

	// 获取用户收藏总数
	public long countUserFavorites(Long userId) {
		return favoriteSongRepository.countByUserId(userId);
	}
}




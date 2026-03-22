package com.music.service;

import com.music.domain.PlayHistory;
import com.music.repository.PlayHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PlayHistoryService {

	private final PlayHistoryRepository playHistoryRepository;

	public PlayHistoryService(PlayHistoryRepository playHistoryRepository) {
		this.playHistoryRepository = playHistoryRepository;
	}

	// 记录播放历史
	public void recordPlay(Long userId, Long songId, Integer playDuration) {
		PlayHistory history = new PlayHistory();
		history.setUserId(userId);
		history.setSongId(songId);
		history.setPlayDuration(playDuration);
		playHistoryRepository.save(history);
	}

	// 获取用户播放历史（分页）
	public Page<PlayHistory> getUserHistory(Long userId, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
		return playHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pr);
	}

	// 获取用户播放总次数
	public long countUserPlays(Long userId) {
		return playHistoryRepository.countByUserId(userId);
	}
}




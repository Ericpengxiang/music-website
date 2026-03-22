package com.music.service;

import com.music.domain.Lyric;
import com.music.service.AuditLogService;
import com.music.repository.LyricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LyricService {

    private final LyricRepository lyricRepository;
    private final AuditLogService auditLogService;

    public LyricService(LyricRepository lyricRepository, AuditLogService auditLogService) {
        this.lyricRepository = lyricRepository;
        this.auditLogService = auditLogService;
    }

	// 获取歌词
	public Optional<Lyric> getBySongId(Long songId) {
		return lyricRepository.findBySongId(songId);
	}

	// 创建或更新歌词
	@Transactional
    public Lyric createOrUpdate(Long songId, String content, String translated) {
        Optional<Lyric> existing = lyricRepository.findBySongId(songId);
        boolean isCreate = existing.isEmpty();
        Lyric lyric = existing.orElseGet(Lyric::new);
        lyric.setSongId(songId);
        lyric.setContent(content);
        if (translated != null) {
            lyric.setTranslated(translated);
        }
        Lyric saved = lyricRepository.save(lyric);
        // 写入操作日志
        try {
            auditLogService.log(isCreate ? "创建" : "更新", "歌词", saved.getId(), (isCreate ? "创建" : "更新") + "歌词 songId=" + songId);
        } catch (Exception ignored) {}
        return saved;
    }

	// 删除歌词
	@Transactional
	public void deleteBySongId(Long songId) {
		lyricRepository.deleteBySongId(songId);
        try {
            auditLogService.log("删除", "歌词", songId, "按songId删除歌词" );
        } catch (Exception ignored) {}
	}

	// 获取所有歌词（管理员用）
	public java.util.List<Lyric> getAllLyrics() {
		return lyricRepository.findAll();
	}
}




package com.music.service;

import com.music.domain.Playlist;
import com.music.domain.PlaylistSong;
import com.music.exception.NotFoundException;
import com.music.repository.PlaylistRepository;
import com.music.repository.PlaylistSongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;

    public PlaylistService(PlaylistRepository playlistRepository, PlaylistSongRepository playlistSongRepository) {
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
    }

    public List<Playlist> listMy(Long userId) {
        return playlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Playlist create(Long userId, String name, String description, Boolean isPublic) {
        Playlist p = new Playlist();
        p.setUserId(userId);
        p.setName(name);
        p.setDescription(description);
        p.setPublic(isPublic != null ? isPublic : true);
        return playlistRepository.save(p);
    }

    public Playlist getOwned(Long userId, Long playlistId) {
        Playlist p = playlistRepository.findById(playlistId).orElseThrow(() -> new NotFoundException("歌单不存在"));
        if (!p.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该歌单");
        }
        return p;
    }

    @Transactional
    public void delete(Long userId, Long playlistId) {
        Playlist p = getOwned(userId, playlistId);
        playlistRepository.delete(p);
    }

    @Transactional
    public void addSong(Long userId, Long playlistId, Long songId) {
        getOwned(userId, playlistId);
        if (playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId).isPresent()) return;
        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylistId(playlistId);
        ps.setSongId(songId);
        ps.setSortOrder(0);
        playlistSongRepository.save(ps);
    }

    @Transactional
    public void removeSong(Long userId, Long playlistId, Long songId) {
        getOwned(userId, playlistId);
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    public List<PlaylistSong> getSongs(Long userId, Long playlistId) {
        getOwned(userId, playlistId);
        return playlistSongRepository.findByPlaylistIdOrderBySortOrderAsc(playlistId);
    }
}



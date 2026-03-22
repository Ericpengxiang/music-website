package com.music.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "play_history")
public class PlayHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "song_id", nullable = false, insertable = false, updatable = false)
	private Song song;

	@Column(name = "song_id", nullable = false)
	private Long songId;

	@Column(name = "play_duration")
	private Integer playDuration;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public Song getSong() { return song; }
	public void setSong(Song song) { this.song = song; }

	public Long getSongId() { return songId; }
	public void setSongId(Long songId) { this.songId = songId; }

	public Integer getPlayDuration() { return playDuration; }
	public void setPlayDuration(Integer playDuration) { this.playDuration = playDuration; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}



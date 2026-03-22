package com.music.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "music_story")
public class MusicStory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "song_id", nullable = false)
	private Long songId;

	@Column(name = "story", nullable = false, columnDefinition = "TEXT")
	private String story;

	@Column(name = "emotion", length = 20)
	private String emotion;

	@Column(name = "like_count", nullable = false)
	private int likeCount = 0;

	@Column(name = "is_featured", nullable = false)
	private boolean featured = false;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// 关联用户信息（用于显示）
	@Transient
	private String username;

	@Transient
	private String userAvatar;

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public Long getSongId() { return songId; }
	public void setSongId(Long songId) { this.songId = songId; }

	public String getStory() { return story; }
	public void setStory(String story) { this.story = story; }

	public String getEmotion() { return emotion; }
	public void setEmotion(String emotion) { this.emotion = emotion; }

	public int getLikeCount() { return likeCount; }
	public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

	public boolean isFeatured() { return featured; }
	public void setFeatured(boolean featured) { this.featured = featured; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getUserAvatar() { return userAvatar; }
	public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
}






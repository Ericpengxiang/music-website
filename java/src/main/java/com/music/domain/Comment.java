package com.music.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "comment")
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "resource_type", nullable = false, length = 20)
	private String resourceType;

	@Column(name = "resource_id", nullable = false)
	private Long resourceId;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "parent_id")
	private Long parentId;

	@Column(name = "like_count", nullable = false)
	private int likeCount = 0;

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

	public String getResourceType() { return resourceType; }
	public void setResourceType(String resourceType) { this.resourceType = resourceType; }

	public Long getResourceId() { return resourceId; }
	public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }

	public Long getParentId() { return parentId; }
	public void setParentId(Long parentId) { this.parentId = parentId; }

	public int getLikeCount() { return likeCount; }
	public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getUserAvatar() { return userAvatar; }
	public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
}






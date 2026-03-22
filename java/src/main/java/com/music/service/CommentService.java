package com.music.service;

import com.music.domain.Comment;
import com.music.repository.CommentLikeRepository;
import com.music.repository.CommentRepository;
import com.music.repository.FrontendUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CommentService {

	private final CommentRepository commentRepository;
	private final CommentLikeRepository commentLikeRepository;
	private final FrontendUserRepository userRepository;

	public CommentService(CommentRepository commentRepository, 
	                      CommentLikeRepository commentLikeRepository,
	                      FrontendUserRepository userRepository) {
		this.commentRepository = commentRepository;
		this.commentLikeRepository = commentLikeRepository;
		this.userRepository = userRepository;
	}

	// 发布评论
	@Transactional
	public Comment create(Long userId, String resourceType, Long resourceId, String content, Long parentId) {
		Comment comment = new Comment();
		comment.setUserId(userId);
		comment.setResourceType(resourceType);
		comment.setResourceId(resourceId);
		comment.setContent(content);
		comment.setParentId(parentId);
		return commentRepository.save(comment);
	}

	// 获取资源的评论（分页，热门优先）
	public Page<Comment> getComments(String resourceType, Long resourceId, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
		Page<Comment> comments = commentRepository.findByResourceTypeAndResourceIdAndParentIdIsNullOrderByLikeCountDescCreatedAtDesc(
				resourceType, resourceId, pr);
		
		// 批量填充用户信息（优化N+1查询）
		fillUserInfoBatch(comments.getContent());
		return comments;
	}

	// 获取回复
	public List<Comment> getReplies(Long commentId) {
		List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
		fillUserInfoBatch(replies);
		return replies;
	}

	// 批量填充用户信息（优化N+1查询）
	private void fillUserInfoBatch(List<Comment> comments) {
		if (comments.isEmpty()) return;
		
		// 收集所有用户ID
		Set<Long> userIds = comments.stream()
				.map(Comment::getUserId)
				.collect(java.util.stream.Collectors.toSet());
		
		// 批量查询用户信息
		Map<Long, com.music.domain.FrontendUser> userMap = userRepository.findAllById(userIds).stream()
				.collect(java.util.stream.Collectors.toMap(
						com.music.domain.FrontendUser::getId,
						user -> user
				));
		
		// 填充用户信息
		comments.forEach(comment -> {
			com.music.domain.FrontendUser user = userMap.get(comment.getUserId());
			if (user != null) {
				comment.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
				comment.setUserAvatar(user.getAvatarUrl());
			}
		});
	}

	// 删除评论
	@Transactional
	public void delete(Long userId, Long commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new IllegalArgumentException("评论不存在"));
		if (!comment.getUserId().equals(userId)) {
			throw new IllegalArgumentException("无权删除");
		}
		commentRepository.delete(comment);
	}

	// 点赞评论
	@Transactional
	public void like(Long userId, Long commentId) {
		if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
			return; // 已点赞
		}
		com.music.domain.CommentLike like = new com.music.domain.CommentLike();
		like.setUserId(userId);
		like.setCommentId(commentId);
		commentLikeRepository.save(like);
		commentRepository.incrementLikeCount(commentId);
	}

	// 取消点赞
	@Transactional
	public void unlike(Long userId, Long commentId) {
		commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
		commentRepository.decrementLikeCount(commentId);
	}

	// 检查是否已点赞
	public boolean isLiked(Long userId, Long commentId) {
		return commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
	}

	// 批量检查点赞状态
	public List<Long> getLikedCommentIds(Long userId) {
		return commentLikeRepository.findCommentIdsByUserId(userId);
	}

	// 统计评论数
	public long count(String resourceType, Long resourceId) {
		return commentRepository.countByResourceTypeAndResourceId(resourceType, resourceId);
	}
}


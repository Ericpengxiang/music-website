package com.music.repository;

import com.music.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	// 获取资源的评论（分页）
	Page<Comment> findByResourceTypeAndResourceIdAndParentIdIsNullOrderByLikeCountDescCreatedAtDesc(
			String resourceType, Long resourceId, Pageable pageable);
	
	// 获取某评论的回复
	List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
	
	// 统计资源的评论数
	long countByResourceTypeAndResourceId(String resourceType, Long resourceId);
	
	// 增加点赞数
	@Modifying
	@Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
	void incrementLikeCount(Long commentId);
	
	// 减少点赞数
	@Modifying
	@Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
	void decrementLikeCount(Long commentId);
}






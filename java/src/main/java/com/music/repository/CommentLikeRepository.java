package com.music.repository;

import com.music.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
	Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
	boolean existsByUserIdAndCommentId(Long userId, Long commentId);
	void deleteByUserIdAndCommentId(Long userId, Long commentId);
	
	@Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.userId = :userId")
	List<Long> findCommentIdsByUserId(Long userId);
}






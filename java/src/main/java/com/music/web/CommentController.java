package com.music.web;

import com.music.domain.Comment;
import com.music.security.JwtTokenService;
import com.music.service.CommentService;
import com.music.service.FrontendUserService;
import com.music.web.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

	private final CommentService commentService;
	private final FrontendUserService userService;
	private final JwtTokenService jwtTokenService;

	public CommentController(CommentService commentService, FrontendUserService userService, JwtTokenService jwtTokenService) {
		this.commentService = commentService;
		this.userService = userService;
		this.jwtTokenService = jwtTokenService;
	}

	// 获取资源的评论列表
	@GetMapping
	public PageResponse<Comment> list(@RequestParam(name = "resourceType") String resourceType,
	                                  @RequestParam(name = "resourceId") Long resourceId,
	                                  @RequestParam(name = "page", defaultValue = "0") int page,
	                                  @RequestParam(name = "size", defaultValue = "20") int size) {
		Page<Comment> p = commentService.getComments(resourceType, resourceId, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	// 获取评论的回复
	@GetMapping("/{commentId}/replies")
	public List<Comment> replies(@PathVariable Long commentId) {
		return commentService.getReplies(commentId);
	}

	// 发布评论
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Comment create(@RequestBody Map<String, Object> req, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		String resourceType = (String) req.get("resourceType");
		Long resourceId = Long.valueOf(req.get("resourceId").toString());
		String content = (String) req.get("content");
		Long parentId = req.get("parentId") != null ? Long.valueOf(req.get("parentId").toString()) : null;
		
		return commentService.create(userId, resourceType, resourceId, content, parentId);
	}

	// 删除评论
	@DeleteMapping("/{commentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long commentId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		commentService.delete(userId, commentId);
	}

	// 点赞评论
	@PostMapping("/{commentId}/like")
	@ResponseStatus(HttpStatus.CREATED)
	public void like(@PathVariable Long commentId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		commentService.like(userId, commentId);
	}

	// 取消点赞
	@DeleteMapping("/{commentId}/like")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void unlike(@PathVariable Long commentId, @RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		commentService.unlike(userId, commentId);
	}

	// 检查点赞状态
	@GetMapping("/likes/check")
	public Map<String, List<Long>> checkLikes(@RequestHeader("Authorization") String authHeader) {
		Long userId = getUserIdFromToken(authHeader);
		List<Long> likedIds = commentService.getLikedCommentIds(userId);
		return Map.of("likedCommentIds", likedIds);
	}

	// 统计评论数
	@GetMapping("/count")
	public Map<String, Long> count(@RequestParam(name = "resourceType") String resourceType,
	                                @RequestParam(name = "resourceId") Long resourceId) {
		long count = commentService.count(resourceType, resourceId);
		return Map.of("count", count);
	}

	private Long getUserIdFromToken(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String username = jwtTokenService.extractUsername(token);
		return userService.findByUsername(username).getId();
	}
}






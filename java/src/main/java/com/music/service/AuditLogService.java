package com.music.service;

import com.music.domain.AuditLog;
import com.music.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	public void log(String operation, String resourceType, Long resourceId, String details) {
		AuditLog log = new AuditLog();
		log.setOperation(operation); // 如：创建/更新/删除
		log.setResourceType(resourceType); // 如：歌手/专辑/歌曲
		log.setResourceId(resourceId);
		log.setDetails(details);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			log.setUsername(auth.getName());
		} else {
			log.setUsername("anonymous");
		}

		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs != null) {
			HttpServletRequest request = attrs.getRequest();
			String ip = request.getHeader("X-Forwarded-For");
			if (ip == null || ip.isEmpty()) {
				ip = request.getRemoteAddr();
			}
			log.setIpAddress(ip);
		}

		auditLogRepository.save(log);
	}

	public Page<AuditLog> page(String keyword, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "createdAt"));
		if (keyword == null || keyword.isBlank()) {
			return auditLogRepository.findAll(pr);
		}
		return auditLogRepository.findByUsernameContainingIgnoreCase(keyword.trim(), pr);
	}

	public void delete(Long id) {
		auditLogRepository.deleteById(id);
	}

	public void deleteBatch(List<Long> ids) {
		if (ids == null || ids.isEmpty()) return;
		auditLogRepository.deleteAllByIdInBatch(ids);
	}
}







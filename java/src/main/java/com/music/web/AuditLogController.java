package com.music.web;

import com.music.domain.AuditLog;
import com.music.service.AuditLogService;
import com.music.web.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

	private final AuditLogService auditLogService;

	public AuditLogController(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AuditLog> page(@RequestParam(name = "keyword", required = false) String keyword,
                                       @RequestParam(name = "page", defaultValue = "0") int page,
                                       @RequestParam(name = "size", defaultValue = "20") int size) {
		Page<AuditLog> p = auditLogService.page(keyword, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        auditLogService.delete(id);
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBatch(@RequestBody List<Long> ids) {
        auditLogService.deleteBatch(ids);
    }
}






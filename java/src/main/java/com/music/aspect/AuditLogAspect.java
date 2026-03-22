package com.music.aspect;

import com.music.service.AuditLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

	private final AuditLogService auditLogService;

	public AuditLogAspect(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@AfterReturning("execution(* com.music.service.*.create(..))")
	public void logCreate(JoinPoint jp) {
        String serviceName = jp.getTarget().getClass().getSimpleName();
        String resourceTypeEn = serviceName.replace("Service", "");
        String resourceTypeZh = toChineseResource(resourceTypeEn);
        auditLogService.log("创建", resourceTypeZh, null, "创建" + resourceTypeZh);
	}

	@AfterReturning("execution(* com.music.service.*.update(..))")
	public void logUpdate(JoinPoint jp) {
        String serviceName = jp.getTarget().getClass().getSimpleName();
        String resourceType = toChineseResource(serviceName.replace("Service", ""));
		Object[] args = jp.getArgs();
		Long id = args.length > 0 && args[0] instanceof Long ? (Long) args[0] : null;
        auditLogService.log("更新", resourceType, id, "更新" + resourceType);
	}

	@AfterReturning("execution(* com.music.service.*.delete(..))")
	public void logDelete(JoinPoint jp) {
        String serviceName = jp.getTarget().getClass().getSimpleName();
        String resourceType = toChineseResource(serviceName.replace("Service", ""));
		Object[] args = jp.getArgs();
		Long id = args.length > 0 && args[0] instanceof Long ? (Long) args[0] : null;
        auditLogService.log("删除", resourceType, id, "删除" + resourceType);
	}

    private String toChineseResource(String resourceTypeEn) {
        if (resourceTypeEn == null) return "";
        return switch (resourceTypeEn) {
            case "Artist" -> "歌手";
            case "Album" -> "专辑";
            case "Song" -> "歌曲";
            case "Genre" -> "流派";
            case "User", "AppUser" -> "用户";
            case "Role" -> "角色";
            case "Dashboard" -> "仪表盘";
            default -> resourceTypeEn; // 未知类型保持原样
        };
    }
}







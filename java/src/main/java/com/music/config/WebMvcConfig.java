package com.music.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 获取当前工作目录的绝对路径
		String currentPath = System.getProperty("user.dir");
		String uploadPath = "file:" + currentPath + File.separator + "uploads" + File.separator;
		
		log.info("Configuring upload path: {}", uploadPath);
		
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(uploadPath);
	}

	@Override
	public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOriginPatterns("*")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true)
				.maxAge(3600);
		
		// 允许访问 uploads 目录
		registry.addMapping("/uploads/**")
				.allowedOriginPatterns("*")
				.allowedMethods("GET")
				.allowCredentials(true);
	}
}



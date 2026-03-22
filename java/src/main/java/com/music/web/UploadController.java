package com.music.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

	private static final Logger log = LoggerFactory.getLogger(UploadController.class);
	private static final String UPLOAD_DIR = "uploads";

	@PostMapping
	public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
		try {
			// 验证文件
			if (file.isEmpty()) {
				log.warn("Upload failed: empty file");
				return ResponseEntity.badRequest().body(Map.of("error", "文件为空"));
			}

			// 获取项目根目录的绝对路径
			String currentPath = System.getProperty("user.dir");
			Path uploadDir = Paths.get(currentPath, UPLOAD_DIR);
			
			// 创建上传目录
			Files.createDirectories(uploadDir);
			log.info("Upload directory: {}", uploadDir.toAbsolutePath());

			// 生成唯一文件名
			String originalFilename = file.getOriginalFilename();
			String ext = StringUtils.getFilenameExtension(originalFilename);
			String filename = UUID.randomUUID().toString().replace("-", "");
			if (ext != null && !ext.isEmpty()) {
				filename = filename + "." + ext;
			}

			// 保存文件
			Path target = uploadDir.resolve(filename);
			file.transferTo(target.toFile());
			
			log.info("File uploaded successfully: {} -> {}", originalFilename, filename);

			// 返回访问URL
			Map<String, String> resp = new HashMap<>();
			resp.put("url", "/uploads/" + filename);
			resp.put("filename", filename);
			return ResponseEntity.status(HttpStatus.CREATED).body(resp);

		} catch (IOException e) {
			log.error("Upload failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "上传失败: " + e.getMessage()));
		}
	}
}




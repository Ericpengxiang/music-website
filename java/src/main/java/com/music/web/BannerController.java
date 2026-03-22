package com.music.web;

import com.music.domain.Banner;
import com.music.service.BannerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

	private final BannerService bannerService;

	public BannerController(BannerService bannerService) {
		this.bannerService = bannerService;
	}

	// ========== 前台接口 ==========

	// 获取所有启用的轮播图
	@GetMapping("/active")
	public List<Banner> getActiveBanners() {
		return bannerService.getActiveBanners();
	}

	// ========== 后台管理接口 ==========

	// 获取所有轮播图
	@GetMapping
	public List<Banner> getAllBanners() {
		return bannerService.getAllBanners();
	}

	// 根据ID获取
	@GetMapping("/{id}")
	public Banner getById(@PathVariable Long id) {
		return bannerService.getById(id);
	}

	// 创建
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Banner create(@RequestBody Banner banner) {
		return bannerService.create(banner);
	}

	// 更新
	@PutMapping("/{id}")
	public Banner update(@PathVariable Long id, @RequestBody Banner banner) {
		return bannerService.update(id, banner);
	}

	// 删除
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		bannerService.delete(id);
	}

	// 切换启用状态
	@PutMapping("/{id}/toggle")
	public Banner toggleActive(@PathVariable Long id) {
		return bannerService.toggleActive(id);
	}
}



package com.music.service;

import com.music.domain.Banner;
import com.music.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BannerService {

	private final BannerRepository bannerRepository;

	public BannerService(BannerRepository bannerRepository) {
		this.bannerRepository = bannerRepository;
	}

	// 获取所有启用的轮播图（前台使用）
	public List<Banner> getActiveBanners() {
		return bannerRepository.findByActiveTrueOrderBySortOrderAsc();
	}

	// 获取所有轮播图（后台管理）
	public List<Banner> getAllBanners() {
		return bannerRepository.findAllByOrderBySortOrderAsc();
	}

	// 根据ID获取
	public Banner getById(Long id) {
		return bannerRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("轮播图不存在"));
	}

	// 创建
	@Transactional
	public Banner create(Banner banner) {
		return bannerRepository.save(banner);
	}

	// 更新
	@Transactional
	public Banner update(Long id, Banner banner) {
		Banner existing = getById(id);
		if (banner.getTitle() != null) {
			existing.setTitle(banner.getTitle());
		}
		if (banner.getImageUrl() != null) {
			existing.setImageUrl(banner.getImageUrl());
		}
		if (banner.getLinkUrl() != null) {
			existing.setLinkUrl(banner.getLinkUrl());
		}
		if (banner.getSortOrder() != null) {
			existing.setSortOrder(banner.getSortOrder());
		}
		if (banner.getActive() != null) {
			existing.setActive(banner.getActive());
		}
		return bannerRepository.save(existing);
	}

	// 删除
	@Transactional
	public void delete(Long id) {
		bannerRepository.deleteById(id);
	}

	// 切换启用状态
	@Transactional
	public Banner toggleActive(Long id) {
		Banner banner = getById(id);
		banner.setActive(!banner.getActive());
		return bannerRepository.save(banner);
	}
}



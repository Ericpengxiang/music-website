package com.music.repository;

import com.music.domain.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
	// 获取所有启用的轮播图，按排序字段排序
	List<Banner> findByActiveTrueOrderBySortOrderAsc();
	
	// 获取所有轮播图，按排序字段排序
	List<Banner> findAllByOrderBySortOrderAsc();
}



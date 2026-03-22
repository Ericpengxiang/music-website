package com.music.repository;

import com.music.domain.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Page<Album> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);
}




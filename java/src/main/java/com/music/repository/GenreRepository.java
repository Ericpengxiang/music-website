package com.music.repository;

import com.music.domain.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Page<Genre> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
}




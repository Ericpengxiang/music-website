package com.music.repository;

import com.music.domain.Song;
import com.music.web.dto.CountItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

	Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);

	@Query("select g.name as name, count(s.id) as count from Song s join s.genre g group by g.name order by count(s.id) desc")
	List<CountItem> countByGenre();

	@Query("select a.name as name, count(s.id) as count from Song s join s.artist a group by a.name order by count(s.id) desc")
	List<CountItem> countByArtist();
}




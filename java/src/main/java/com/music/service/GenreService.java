package com.music.service;

import com.music.domain.Genre;
import com.music.exception.NotFoundException;
import com.music.repository.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {

	private final GenreRepository genreRepository;

	public GenreService(GenreRepository genreRepository) {
		this.genreRepository = genreRepository;
	}

	public List<Genre> listAll() {
		return genreRepository.findAll();
	}

	public Genre create(Genre genre) {
		return genreRepository.save(genre);
	}

	public Genre getById(Long id) {
		return genreRepository.findById(id).orElseThrow(() -> new NotFoundException("Genre not found"));
	}

	public Genre update(Long id, Genre update) {
		Genre g = getById(id);
		g.setName(update.getName());
		return genreRepository.save(g);
	}

	public void delete(Long id) {
		Genre g = getById(id);
		genreRepository.delete(g);
	}

    public Page<Genre> page(String keyword, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        if (keyword == null || keyword.isBlank()) {
            return genreRepository.findAll(pr);
        }
        return genreRepository.findByNameContainingIgnoreCase(keyword.trim(), pr);
    }
}




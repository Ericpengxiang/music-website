package com.music.web;

import com.music.domain.Genre;
import com.music.service.GenreService;
import com.music.web.dto.PageResponse;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

	private final GenreService genreService;

	public GenreController(GenreService genreService) {
		this.genreService = genreService;
	}

	@GetMapping
	public List<Genre> list() {
		return genreService.listAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Genre create(@Valid @RequestBody Genre genre) {
		return genreService.create(genre);
	}

	@GetMapping("/{id}")
	public Genre get(@PathVariable Long id) {
		return genreService.getById(id);
	}

	@PutMapping("/{id}")
	public Genre update(@PathVariable Long id, @Valid @RequestBody Genre genre) {
		return genreService.update(id, genre);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		genreService.delete(id);
	}

	@GetMapping("/page")
	public PageResponse<Genre> page(@RequestParam(name = "keyword", required = false) String keyword,
	                               @RequestParam(name = "page", defaultValue = "0") int page,
	                               @RequestParam(name = "size", defaultValue = "10") int size) {
		Page<Genre> p = genreService.page(keyword, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}
}




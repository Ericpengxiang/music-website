package com.music.web;

import com.music.domain.Album;
import com.music.service.AlbumService;
import com.music.web.dto.AlbumRequest;
import com.music.web.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

	private final AlbumService albumService;

	public AlbumController(AlbumService albumService) {
		this.albumService = albumService;
	}

	@GetMapping
	public List<Album> list() { return albumService.listAll(); }

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Album create(@Valid @RequestBody AlbumRequest req) {
		Album a = new Album();
		a.setTitle(req.getTitle());
		a.setCoverUrl(req.getCoverUrl());
		a.setReleaseDate(req.getReleaseDate());
		return albumService.create(a, req.getArtistId());
	}

	@GetMapping("/{id}")
	public Album get(@PathVariable Long id) { return albumService.getById(id); }

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Album update(@PathVariable Long id, @Valid @RequestBody AlbumRequest req) {
		Album a = new Album();
		a.setTitle(req.getTitle());
		a.setCoverUrl(req.getCoverUrl());
		a.setReleaseDate(req.getReleaseDate());
		return albumService.update(id, a, req.getArtistId());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) { albumService.delete(id); }

    @GetMapping("/page")
    public PageResponse<Album> page(@RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "10") int size) {
		Page<Album> p = albumService.page(keyword, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	@DeleteMapping("/batch")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void batchDelete(@RequestBody List<Long> ids) {
		ids.forEach(albumService::delete);
	}
}




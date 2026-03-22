package com.music.web;

import com.music.domain.Artist;
import com.music.service.ArtistService;
import com.music.web.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import com.music.web.dto.ArtistRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

	private final ArtistService artistService;

	public ArtistController(ArtistService artistService) {
		this.artistService = artistService;
	}

	@GetMapping
	public List<Artist> list() { return artistService.listAll(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Artist create(@Valid @RequestBody ArtistRequest req) {
		Artist a = new Artist();
		a.setName(req.getName());
		a.setBio(req.getBio());
		a.setAvatarUrl(req.getAvatarUrl());
		return artistService.create(a);
	}

	@GetMapping("/{id}")
	public Artist get(@PathVariable Long id) { return artistService.getById(id); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	public Artist update(@PathVariable Long id, @Valid @RequestBody ArtistRequest req) {
		Artist a = new Artist();
		a.setName(req.getName());
		a.setBio(req.getBio());
		a.setAvatarUrl(req.getAvatarUrl());
		return artistService.update(id, a);
	}

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) { artistService.delete(id); }

    @GetMapping("/page")
    public PageResponse<Artist> page(@RequestParam(name = "keyword", required = false) String keyword,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<Artist> p = artistService.page(keyword, page, size);
        return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }
}




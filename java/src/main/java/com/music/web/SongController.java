package com.music.web;

import com.music.domain.Song;
import com.music.service.SongService;
import com.music.web.dto.PageResponse;
import com.music.web.dto.SongRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

	private final SongService songService;

	public SongController(SongService songService) {
		this.songService = songService;
	}

	@GetMapping
	public List<Song> list() { return songService.listAll(); }

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Song create(@Valid @RequestBody SongRequest req) {
		Song s = new Song();
		s.setTitle(req.getTitle());
		s.setDurationSec(req.getDurationSec());
		s.setAudioUrl(req.getAudioUrl());
		return songService.create(s, req.getArtistId(), req.getAlbumId(), req.getGenreId());
	}

	@GetMapping("/{id}")
	public Song get(@PathVariable Long id) { return songService.getById(id); }

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Song update(@PathVariable Long id, @Valid @RequestBody SongRequest req) {
		Song s = new Song();
		s.setTitle(req.getTitle());
		s.setDurationSec(req.getDurationSec());
		s.setAudioUrl(req.getAudioUrl());
		return songService.update(id, s, req.getArtistId(), req.getAlbumId(), req.getGenreId());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) { songService.delete(id); }

    @GetMapping("/page")
    public PageResponse<Song> page(@RequestParam(name = "keyword", required = false) String keyword,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size) {
		Page<Song> p = songService.page(keyword, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	@DeleteMapping("/batch")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void batchDelete(@RequestBody List<Long> ids) {
		ids.forEach(songService::delete);
	}

	// 按歌手ID获取歌曲
	@GetMapping("/by-artist/{artistId}")
	public List<Song> byArtist(@PathVariable Long artistId) {
		return songService.getByArtistId(artistId);
	}

	// 按专辑ID获取歌曲
	@GetMapping("/by-album/{albumId}")
	public List<Song> byAlbum(@PathVariable Long albumId) {
		return songService.getByAlbumId(albumId);
	}

	// 按流派ID获取歌曲
	@GetMapping("/by-genre/{genreId}")
	public List<Song> byGenre(@PathVariable Long genreId) {
		return songService.getByGenreId(genreId);
	}
}




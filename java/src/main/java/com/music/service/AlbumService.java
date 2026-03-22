package com.music.service;

import com.music.domain.Album;
import com.music.domain.Artist;
import com.music.exception.NotFoundException;
import com.music.repository.AlbumRepository;
import com.music.repository.ArtistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbumService {

	private final AlbumRepository albumRepository;
	private final ArtistRepository artistRepository;

	public AlbumService(AlbumRepository albumRepository, ArtistRepository artistRepository) {
		this.albumRepository = albumRepository;
		this.artistRepository = artistRepository;
	}

	public List<Album> listAll() { return albumRepository.findAll(); }

	public Album getById(Long id) {
		return albumRepository.findById(id).orElseThrow(() -> new NotFoundException("Album not found"));
	}

	public Album create(Album album, Long artistId) {
		Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new NotFoundException("Artist not found"));
		album.setArtist(artist);
		return albumRepository.save(album);
	}

	public Album update(Long id, Album update, Long artistId) {
		Album a = getById(id);
		if (artistId != null) {
			Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new NotFoundException("Artist not found"));
			a.setArtist(artist);
		}
		a.setTitle(update.getTitle());
		a.setCoverUrl(update.getCoverUrl());
		a.setReleaseDate(update.getReleaseDate());
		return albumRepository.save(a);
	}

	public void delete(Long id) {
		Album a = getById(id);
		albumRepository.delete(a);
	}

	public Page<Album> page(String keyword, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
		if (keyword == null || keyword.isBlank()) {
			return albumRepository.findAll(pr);
		}
		return albumRepository.findByTitleContainingIgnoreCase(keyword.trim(), pr);
	}
}




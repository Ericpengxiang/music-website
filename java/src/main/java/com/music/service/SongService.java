package com.music.service;

import com.music.domain.*;
import com.music.exception.NotFoundException;
import com.music.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {

	private final SongRepository songRepository;
	private final ArtistRepository artistRepository;
	private final AlbumRepository albumRepository;
	private final GenreRepository genreRepository;

	public SongService(SongRepository songRepository, ArtistRepository artistRepository, AlbumRepository albumRepository, GenreRepository genreRepository) {
		this.songRepository = songRepository;
		this.artistRepository = artistRepository;
		this.albumRepository = albumRepository;
		this.genreRepository = genreRepository;
	}

	public List<Song> listAll() { return songRepository.findAll(); }

	public Song getById(Long id) { return songRepository.findById(id).orElseThrow(() -> new NotFoundException("Song not found")); }

	public Song create(Song song, Long artistId, Long albumId, Long genreId) {
		Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new NotFoundException("Artist not found"));
		song.setArtist(artist);
		if (albumId != null) {
			Album album = albumRepository.findById(albumId).orElseThrow(() -> new NotFoundException("Album not found"));
			song.setAlbum(album);
		}
		if (genreId != null) {
			Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new NotFoundException("Genre not found"));
			song.setGenre(genre);
		}
		return songRepository.save(song);
	}

	public Song update(Long id, Song update, Long artistId, Long albumId, Long genreId) {
		Song s = getById(id);
		if (artistId != null) {
			Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new NotFoundException("Artist not found"));
			s.setArtist(artist);
		}
		if (albumId != null) {
			Album album = albumRepository.findById(albumId).orElseThrow(() -> new NotFoundException("Album not found"));
			s.setAlbum(album);
		} else {
			s.setAlbum(null);
		}
		if (genreId != null) {
			Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new NotFoundException("Genre not found"));
			s.setGenre(genre);
		} else {
			s.setGenre(null);
		}
		s.setTitle(update.getTitle());
		s.setDurationSec(update.getDurationSec());
		s.setAudioUrl(update.getAudioUrl());
		return songRepository.save(s);
	}

	public void delete(Long id) { songRepository.delete(getById(id)); }

	public Page<Song> page(String keyword, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
		if (keyword == null || keyword.isBlank()) {
			return songRepository.findAll(pr);
		}
		return songRepository.findByTitleContainingIgnoreCase(keyword.trim(), pr);
	}

	// 按歌手ID获取歌曲
	public List<Song> getByArtistId(Long artistId) {
		return songRepository.findAll().stream()
				.filter(s -> s.getArtist() != null && s.getArtist().getId().equals(artistId))
				.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
				.collect(java.util.stream.Collectors.toList());
	}

	// 按专辑ID获取歌曲
	public List<Song> getByAlbumId(Long albumId) {
		return songRepository.findAll().stream()
				.filter(s -> s.getAlbum() != null && s.getAlbum().getId().equals(albumId))
				.sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
				.collect(java.util.stream.Collectors.toList());
	}

	// 按流派ID获取歌曲
	public List<Song> getByGenreId(Long genreId) {
		return songRepository.findAll().stream()
				.filter(s -> s.getGenre() != null && s.getGenre().getId().equals(genreId))
				.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
				.collect(java.util.stream.Collectors.toList());
	}
}




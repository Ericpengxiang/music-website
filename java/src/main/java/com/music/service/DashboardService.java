package com.music.service;

import com.music.repository.*;
import com.music.web.dto.DashboardStats;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

	private final GenreRepository genreRepository;
	private final ArtistRepository artistRepository;
	private final AlbumRepository albumRepository;
	private final SongRepository songRepository;
	private final AppUserRepository userRepository;

	public DashboardService(GenreRepository genreRepository, ArtistRepository artistRepository,
	                        AlbumRepository albumRepository, SongRepository songRepository,
	                        AppUserRepository userRepository) {
		this.genreRepository = genreRepository;
		this.artistRepository = artistRepository;
		this.albumRepository = albumRepository;
		this.songRepository = songRepository;
		this.userRepository = userRepository;
	}

	public DashboardStats getStats() {
		DashboardStats stats = new DashboardStats();
		stats.setGenreCount(genreRepository.count());
		stats.setArtistCount(artistRepository.count());
		stats.setAlbumCount(albumRepository.count());
		stats.setSongCount(songRepository.count());
		stats.setUserCount(userRepository.count());
		stats.setSongsByGenre(songRepository.countByGenre());
		stats.setSongsByArtist(songRepository.countByArtist());
		return stats;
	}
}





























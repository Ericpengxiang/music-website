package com.music.service;

import com.music.domain.Artist;
import com.music.exception.NotFoundException;
import com.music.repository.ArtistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistService {

	private final ArtistRepository artistRepository;

	public ArtistService(ArtistRepository artistRepository) {
		this.artistRepository = artistRepository;
	}

	public List<Artist> listAll() { return artistRepository.findAll(); }

	public Artist create(Artist artist) { return artistRepository.save(artist); }

	public Artist getById(Long id) {
		return artistRepository.findById(id).orElseThrow(() -> new NotFoundException("Artist not found"));
	}

	public Artist update(Long id, Artist update) {
		Artist a = getById(id);
		a.setName(update.getName());
		a.setBio(update.getBio());
		a.setAvatarUrl(update.getAvatarUrl());
		return artistRepository.save(a);
	}

	public void delete(Long id) {
		Artist a = getById(id);
		artistRepository.delete(a);
	}

    public Page<Artist> page(String keyword, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 200));
        if (keyword == null || keyword.isBlank()) {
            return artistRepository.findAll(pr);
        }
        return artistRepository.findByNameContainingIgnoreCase(keyword.trim(), pr);
    }
}




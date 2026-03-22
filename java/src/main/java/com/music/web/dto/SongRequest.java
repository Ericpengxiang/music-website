package com.music.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SongRequest {
	@NotBlank
	private String title;
	@NotNull
	private Long artistId;
	private Long albumId;
	private Long genreId;
	private Integer durationSec;
	private String audioUrl;

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public Long getArtistId() { return artistId; }
	public void setArtistId(Long artistId) { this.artistId = artistId; }
	public Long getAlbumId() { return albumId; }
	public void setAlbumId(Long albumId) { this.albumId = albumId; }
	public Long getGenreId() { return genreId; }
	public void setGenreId(Long genreId) { this.genreId = genreId; }
	public Integer getDurationSec() { return durationSec; }
	public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
	public String getAudioUrl() { return audioUrl; }
	public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
}




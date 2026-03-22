package com.music.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AlbumRequest {
	@NotBlank
	private String title;
	@NotNull
	private Long artistId;
	private String coverUrl;
	private LocalDate releaseDate;

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public Long getArtistId() { return artistId; }
	public void setArtistId(Long artistId) { this.artistId = artistId; }
	public String getCoverUrl() { return coverUrl; }
	public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
	public LocalDate getReleaseDate() { return releaseDate; }
	public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
}




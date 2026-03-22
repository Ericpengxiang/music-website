package com.music.web.dto;

import jakarta.validation.constraints.NotBlank;

public class ArtistRequest {
	@NotBlank
	private String name;
	private String bio;
	private String avatarUrl;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getBio() { return bio; }
	public void setBio(String bio) { this.bio = bio; }
	public String getAvatarUrl() { return avatarUrl; }
	public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}




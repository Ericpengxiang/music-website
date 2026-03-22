package com.music.web.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRequest {
	@NotBlank
	private String username;
	private String displayName;
	private String password; // optional for update
	private Boolean active; // optional for update

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public Boolean getActive() { return active; }
	public void setActive(Boolean active) { this.active = active; }
}




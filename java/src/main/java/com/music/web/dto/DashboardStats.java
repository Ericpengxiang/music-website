package com.music.web.dto;

import java.util.List;

public class DashboardStats {
    private long genreCount;
    private long artistCount;
    private long albumCount;
    private long songCount;
    private long userCount;
    private List<CountItem> songsByGenre;
    private List<CountItem> songsByArtist;

    public long getGenreCount() { return genreCount; }
    public void setGenreCount(long genreCount) { this.genreCount = genreCount; }
    public long getArtistCount() { return artistCount; }
    public void setArtistCount(long artistCount) { this.artistCount = artistCount; }
    public long getAlbumCount() { return albumCount; }
    public void setAlbumCount(long albumCount) { this.albumCount = albumCount; }
    public long getSongCount() { return songCount; }
    public void setSongCount(long songCount) { this.songCount = songCount; }
    public long getUserCount() { return userCount; }
    public void setUserCount(long userCount) { this.userCount = userCount; }
    public List<CountItem> getSongsByGenre() { return songsByGenre; }
    public void setSongsByGenre(List<CountItem> songsByGenre) { this.songsByGenre = songsByGenre; }
    public List<CountItem> getSongsByArtist() { return songsByArtist; }
    public void setSongsByArtist(List<CountItem> songsByArtist) { this.songsByArtist = songsByArtist; }
}



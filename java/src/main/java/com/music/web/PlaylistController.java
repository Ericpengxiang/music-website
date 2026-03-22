package com.music.web;

import com.music.domain.Playlist;
import com.music.security.JwtTokenService;
import com.music.service.FrontendUserService;
import com.music.service.PlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final FrontendUserService userService;
    private final JwtTokenService jwtTokenService;

    public PlaylistController(PlaylistService playlistService, FrontendUserService userService, JwtTokenService jwtTokenService) {
        this.playlistService = playlistService;
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping
    public List<Playlist> myPlaylists(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return playlistService.listMy(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Playlist create(@RequestBody Map<String, Object> req, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        String name = String.valueOf(req.getOrDefault("name", "新建歌单"));
        String description = (String) req.get("description");
        Boolean isPublic = req.get("isPublic") != null ? Boolean.valueOf(String.valueOf(req.get("isPublic"))) : null;
        return playlistService.create(userId, name, description, isPublic);
    }

    @DeleteMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long playlistId, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        playlistService.delete(userId, playlistId);
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addSong(@PathVariable Long playlistId, @PathVariable Long songId, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        playlistService.addSong(userId, playlistId, songId);
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSong(@PathVariable Long playlistId, @PathVariable Long songId, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        playlistService.removeSong(userId, playlistId, songId);
    }

    @GetMapping("/{playlistId}/songs")
    public List<?> getSongs(@PathVariable Long playlistId, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return playlistService.getSongs(userId, playlistId);
    }

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtTokenService.extractUsername(token);
        return userService.findByUsername(username).getId();
    }
}



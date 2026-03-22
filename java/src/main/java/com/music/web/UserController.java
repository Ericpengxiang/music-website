package com.music.web;

import com.music.domain.AppUser;
import com.music.service.UserService;
import com.music.web.dto.PageResponse;
import com.music.web.dto.UserRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<AppUser> list() { return userService.listAll(); }

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public AppUser create(@Valid @RequestBody UserRequest req) {
		AppUser u = new AppUser();
		u.setUsername(req.getUsername());
		u.setDisplayName(req.getDisplayName());
		u.setActive(req.getActive() == null ? true : req.getActive());
		return userService.create(u, req.getPassword());
	}

	@GetMapping("/{id}")
	public AppUser get(@PathVariable Long id) { return userService.getById(id); }

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AppUser update(@PathVariable Long id, @Valid @RequestBody UserRequest req) {
		AppUser u = new AppUser();
		u.setDisplayName(req.getDisplayName());
		u.setActive(req.getActive() == null ? true : req.getActive());
		return userService.update(id, u, req.getPassword());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) { userService.delete(id); }

    @GetMapping("/page")
    public PageResponse<AppUser> page(@RequestParam(name = "keyword", required = false) String keyword,
                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "10") int size) {
		Page<AppUser> p = userService.page(keyword, page, size);
		return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
	}

	@DeleteMapping("/batch")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void batchDelete(@RequestBody List<Long> ids) {
		ids.forEach(userService::delete);
	}
}




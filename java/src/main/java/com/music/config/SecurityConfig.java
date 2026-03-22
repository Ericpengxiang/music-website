package com.music.config;

import com.music.security.CustomUserDetailsService;
import com.music.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomUserDetailsService userDetailsService;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService userDetailsService) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**").permitAll() // 后台管理员登录
						.requestMatchers("/api/frontend/auth/**").permitAll() // 前台用户注册/登录
						.requestMatchers("/uploads/**").permitAll() // 允许匿名访问静态图片
						.requestMatchers("/api/songs/**").permitAll() // 前台浏览歌曲
						.requestMatchers("/api/albums/**").permitAll() // 前台浏览专辑
						.requestMatchers("/api/artists/**").permitAll() // 前台浏览歌手
						.requestMatchers("/api/genres/**").permitAll() // 前台浏览流派
						.requestMatchers("/api/dashboard/stats").permitAll() // 前台统计数据
                        .requestMatchers("/api/recommend/similar/**").permitAll() // 相似歌曲公开
                        .requestMatchers("/api/banners/active").permitAll() // 首页轮播图公开
						.requestMatchers("/api/recommend/**").authenticated() // 个性化推荐需要登录
						.requestMatchers("/api/ranking/**").permitAll() // 排行榜公开
						.requestMatchers("/api/lyrics/**").permitAll() // 歌词查看公开
						.requestMatchers("/api/comments").permitAll() // 查看评论公开
						.requestMatchers("/api/comments/count").permitAll() // 评论数公开
						.requestMatchers("/api/comments/**").authenticated() // 发布评论需要登录
						.requestMatchers("/api/stories/song/**").permitAll() // 查看故事公开
						.requestMatchers("/api/stories/featured").permitAll() // 精选故事公开
						.requestMatchers("/api/stories/emotion/**").permitAll() // 按情感查看公开
						.requestMatchers("/api/stories/count/**").permitAll() // 故事数公开
						.requestMatchers("/api/stories/**").authenticated() // 发布故事需要登录
						.requestMatchers("/api/favorites/**").authenticated() // 收藏功能需要登录
						.requestMatchers("/api/playlists/**").authenticated() // 歌单需要登录
						.requestMatchers("/api/history/**").authenticated() // 播放历史需要登录
						.anyRequest().authenticated()
				)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(restAuthenticationEntryPoint())
						.accessDeniedHandler(restAccessDeniedHandler())
				)
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public AuthenticationEntryPoint restAuthenticationEntryPoint() {
		return (request, response, authException) -> {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			new ObjectMapper().writeValue(response.getWriter(),
					java.util.Map.of("status", 401, "error", "Unauthorized", "message", authException.getMessage(), "path", request.getRequestURI()));
		};
	}

	@Bean
	public AccessDeniedHandler restAccessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json;charset=UTF-8");
			new ObjectMapper().writeValue(response.getWriter(),
					java.util.Map.of("status", 403, "error", "Forbidden", "message", accessDeniedException.getMessage(), "path", request.getRequestURI()));
		};
	}
}




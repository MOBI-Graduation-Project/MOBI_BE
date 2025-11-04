package com.mobi.mobi.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.mobi.mobi.config.security.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration c = new CorsConfiguration();
                    c.setAllowCredentials(true);

                    // 운영 도메인
                    c.addAllowedOriginPattern("https://mobi.ai.kr");     // 프론트
                    c.addAllowedOriginPattern("https://www.mobi.ai.kr"); // www 사용 시
                    c.addAllowedOriginPattern("https://api.mobi.ai.kr"); // API(CloudFront, Swagger 접근)

                    // 로컬 개발
                    c.addAllowedOriginPattern("http://localhost:3000");
                    c.addAllowedOriginPattern("http://127.0.0.1:3000");
                    c.addAllowedOriginPattern("http://localhost:5173");   // Vite
                    c.addAllowedOriginPattern("http://127.0.0.1:5173");

                    c.addAllowedHeader("*");   // Authorization 등
                    c.addAllowedMethod("*");   // GET/POST/PUT/PATCH/DELETE/OPTIONS
                    // 프론트에서 읽어야 하는 헤더 노출
                    c.addExposedHeader("Authorization");
                    c.addExposedHeader("Location");
                    c.addExposedHeader("Set-Cookie");
                    return c;
                }))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()  // SockJS 핸드셰이크(/ws, /ws/info 등)
                        .requestMatchers("/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


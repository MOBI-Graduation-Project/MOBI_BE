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

                    // --- 운영 도메인 ---
                    c.addAllowedOriginPattern("https://mobi.ai.kr");
                    c.addAllowedOriginPattern("https://www.mobi.ai.kr");
                    c.addAllowedOriginPattern("https://api.mobi.ai.kr"); // 스웨거 UI 접근

                    // --- EB 도메인 (HTTPS 기준, 소문자로) ---
                    c.addAllowedOriginPattern("https://mobi-env.eba-syirxtxr.ap-northeast-2.elasticbeanstalk.com");

                    // --- 로컬 개발 ---
                    c.addAllowedOriginPattern("http://localhost:3000");
                    c.addAllowedOriginPattern("https://localhost:3000");
                    c.addAllowedOriginPattern("http://127.0.0.1:3000");
                    c.addAllowedOriginPattern("http://localhost:5173"); // Vite 등
                    c.addAllowedOriginPattern("http://127.0.0.1:5173");
                    c.addAllowedOriginPattern("http://localhost:8080");
                    c.addAllowedOriginPattern("https://localhost:8080");

                    // --- 모든 메서드/헤더 허용 ---
                    c.addAllowedHeader("*");
                    c.addAllowedMethod("*");

                    // --- 프론트에서 읽어야 하는 헤더 노출 ---
                    c.addExposedHeader("Authorization"); // JWT
                    c.addExposedHeader("Location");      // OAuth2 리다이렉트
                    c.addExposedHeader("Set-Cookie");    // 리프레시 토큰 등

                    return c;
                }))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS 메서드는 언제나 허용 (CORS Preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 스웨거 및 인증 관련 엔드포인트 허용
                        .requestMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // 웹소켓 경로 허용
                        .requestMatchers("/ws/**").permitAll()

                        // 채팅 경로는 인증 필요
                        .requestMatchers("/chat/**").authenticated()

                        // 그 외 모든 요청은 인증 필요
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



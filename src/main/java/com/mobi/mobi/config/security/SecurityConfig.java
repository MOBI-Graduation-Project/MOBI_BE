package com.mobi.mobi.config.security;

import jakarta.servlet.http.HttpServletResponse;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(req -> {
                    CorsConfiguration c = new CorsConfiguration();
                    c.setAllowCredentials(true);
                    c.setAllowedOrigins(List.of(
                            "https://mobi.ai.kr",
                            "https://www.mobi.ai.kr",
                            "https://api.mobi.ai.kr",
                            "http://localhost:3000",
                            "http://127.0.0.1:3000",
                            "http://localhost:5173",
                            "http://127.0.0.1:5173"
                    ));
                    c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
                    c.setAllowedHeaders(List.of("*"));
                    c.setExposedHeaders(List.of("Authorization","Location","Set-Cookie"));
                    c.setMaxAge(3600L);
                    return c;
                }))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**"
                        ).permitAll()

                        // OAuth JSON 플로우 (토큰 발급/리프레시)
                        .requestMatchers("/auth/**").permitAll()

                        // 닉네임 중복확인 API (컨트롤러 경로와 정확히 맞추세요)
                        .requestMatchers(HttpMethod.GET,  "/members/check-nickname").permitAll()
                        .requestMatchers(HttpMethod.POST, "/members/check-nickname").permitAll()

                        // (선택) 헬스체크
                        .requestMatchers(HttpMethod.GET, "/healthz").permitAll()

                        // 웹소켓 핸드셰이크
                        .requestMatchers("/ws/**").permitAll()

                        // 보호 경로
                        .requestMatchers("/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write(
                                    "{\"isSuccess\":false,\"code\":\"AUTH401\",\"message\":\"Unauthorized\"}"
                            );
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write(
                                    "{\"isSuccess\":false,\"code\":\"AUTH403\",\"message\":\"Forbidden\"}"
                            );
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


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
                    c.setAllowedOriginPatterns(List.of(
                            "https://mobi.ai.kr",
                            "https://www.mobi.ai.kr",
                            "https://api.mobi.ai.kr",
                            "https://*.cloudfront.net",
                            "http://mobi-env.eba-syirxtxr.ap-northeast-2.elasticbeanstalk.com",
                            "http://localhost:*",
                            "http://127.0.0.1:*"
                    ));

                    c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
                    c.setAllowedHeaders(List.of("*"));
                    c.setExposedHeaders(List.of("Authorization","Location","Set-Cookie"));
                    c.setMaxAge(3600L);
                    return c;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Preflight 통과
                        .requestMatchers(
                                "/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/auth/**", "/oauth2/**", "/login/**").permitAll() // OAuth 경로 전부 허용
                        .requestMatchers(HttpMethod.GET, "/members/check-nickname").permitAll()
                        .requestMatchers(HttpMethod.POST, "/members/check-nickname").permitAll()
                        .requestMatchers("/healthz").permitAll()
                        .requestMatchers("/ws/**").permitAll()
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


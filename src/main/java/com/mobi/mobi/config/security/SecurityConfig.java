package com.mobi.mobi.config.security;

import com.mobi.mobi.config.security.jwt.JwtAuthenticationFilter;
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

                    // --- EB 도메인 ---
                    c.addAllowedOriginPattern("https://mobi-env.eba-syirxtxr.ap-northeast-2.elasticbeanstalk.com");
                    c.addAllowedOriginPattern("http://mobi-env.eba-syirxtxr.ap-northeast-2.elasticbeanstalk.com");

                    // --- 로컬 개발 ---
                    c.addAllowedOriginPattern("http://localhost:3000");
                    c.addAllowedOriginPattern("https://localhost:3000");
                    c.addAllowedOriginPattern("http://127.0.0.1:3000");
                    c.addAllowedOriginPattern("http://localhost:5173");
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

                //인증 실패/권한 실패 시 리다이렉트 대신 JSON 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 로그인 안 된 상태에서 보호된 API 접근
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"isSuccess\":false," +
                                            "\"code\":\"AUTH401\"," +
                                            "\"message\":\"인증이 필요합니다.\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 로그인은 했지만 권한이 부족할 때
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"isSuccess\":false," +
                                            "\"code\":\"AUTH403\"," +
                                            "\"message\":\"접근 권한이 없습니다.\"}"
                            );
                        })
                )

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

                        //챗본 그냥 누구나 허용
                        .requestMatchers("/chatbot", "/chatbot/**").permitAll()

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
package GraduProject.stock.service.auth;

import GraduProject.stock.entity.user.User;
import GraduProject.stock.repository.user.userRepository;
import GraduProject.stock.util.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final userRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String sub = (String) oAuth2User.getAttribute("sub");

        Optional<User> userOpt = userRepository.findByOauthProviderAndOauthId("google", sub);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtProvider.createAccessToken(user.getId(), user.getEmail());

            // JWT 응답으로 내려주기 (리디렉션 또는 JSON 응답 방식 선택 가능)
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"accessToken\":\"" + token + "\"}");
        }
    }
}

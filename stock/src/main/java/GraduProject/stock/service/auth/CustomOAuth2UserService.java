package GraduProject.stock.service.auth;

import GraduProject.stock.entity.user.User;
import GraduProject.stock.repository.user.userRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final userRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String sub = (String) attributes.get("sub");
        String email = (String) attributes.get("email");

        // DB에 없으면 생성 (닉네임, 투자유형은 추후 complete-profile에서 입력)
        userRepository.findByOauthProviderAndOauthId("google", sub)
                .orElseGet(() -> userRepository.save(User.builder()
                        .oauthProvider("google")
                        .oauthId(sub)
                        .email(email)
                        .isNewUser(true)
                        .build()));

        return oAuth2User;
    }
}

package GraduProject.stock.util;

import GraduProject.stock.dto.auth.GoogleUserInfo;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GoogleApiCaller {

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map body = response.getBody();
            return new GoogleUserInfo(
                    (String) body.get("sub"),
                    (String) body.get("email"),
                    (String) body.get("name")
            );

        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 accessToken이거나 구글 응답 오류", e);
        }
    }
}


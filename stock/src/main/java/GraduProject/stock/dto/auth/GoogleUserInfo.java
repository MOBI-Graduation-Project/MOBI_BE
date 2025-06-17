package GraduProject.stock.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleUserInfo {
    private String sub;
    private String email;
    private String name;
}

package GraduProject.stock.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteProfileRequest {
    private String nickname;
    private String investmentAnswers; // 예: "A-B-A"
}


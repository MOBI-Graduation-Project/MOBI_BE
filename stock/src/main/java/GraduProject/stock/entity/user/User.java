package GraduProject.stock.entity.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oauthProvider;      // google
    private String oauthId;            // ex: sub
    private String email;

    private String nickname;           // 사용자 입력
    private String investmentAnswers;  // "A-B-A" 형태 (3개 응답)
    private boolean isNewUser;
}


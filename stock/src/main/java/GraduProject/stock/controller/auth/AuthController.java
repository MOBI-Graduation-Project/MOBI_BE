package GraduProject.stock.controller.auth;

import GraduProject.stock.dto.auth.CompleteProfileRequest;
import GraduProject.stock.service.auth.AuthService;
import GraduProject.stock.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/complete-profile")
    public ResponseEntity<Void> completeProfile(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody CompleteProfileRequest request
    ) {
        String token = accessToken.replace("Bearer ", "");
        Long userId = jwtProvider.getUserId(token);
        authService.completeProfile(userId, request);
        return ResponseEntity.ok().build();
    }
}

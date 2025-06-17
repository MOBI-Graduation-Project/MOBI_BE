package GraduProject.stock.service.auth;

import GraduProject.stock.dto.auth.CompleteProfileRequest;
import GraduProject.stock.entity.user.User;
import GraduProject.stock.repository.user.userRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final userRepository userRepository;

    public void completeProfile(Long userId, CompleteProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setNickname(request.getNickname());
        user.setInvestmentAnswers(request.getInvestmentAnswers());
        user.setNewUser(false);

        userRepository.save(user);
    }
}





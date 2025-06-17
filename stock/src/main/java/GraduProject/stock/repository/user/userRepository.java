package GraduProject.stock.repository.user;

import GraduProject.stock.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface userRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthProviderAndOauthId(String provider, String oauthId);
    Optional<User> findById(Long id);
}


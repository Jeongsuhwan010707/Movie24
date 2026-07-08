package project.movie24.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.user.domain.Provider;
import project.movie24.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByName(String name);

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}

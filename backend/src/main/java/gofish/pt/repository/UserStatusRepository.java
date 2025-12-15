package gofish.pt.repository;

import gofish.pt.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {

    Optional<UserStatus> findByUserId(Long userId);

    List<UserStatus> findAllByStatus(String status);

    boolean existsByUserId(Long userId);
}

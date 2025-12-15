package gofish.pt.repository;

import gofish.pt.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByUserId(Long userId);

    List<UserRole> findAllByRole(String role);

    boolean existsByUserId(Long userId);
}

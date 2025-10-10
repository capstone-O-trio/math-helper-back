package cap.math.repository;

import cap.math.domain.Math;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MathRepository extends JpaRepository<Math, Long> {
    Optional<Math> findFirstByUserIdAndIsCheckedFalseOrderByCreatedAtDesc(Long userId);

}

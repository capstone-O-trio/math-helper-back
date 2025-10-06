package cap.math.repository;

import cap.math.domain.Math;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MathRepository extends JpaRepository<Math, Long> {
}

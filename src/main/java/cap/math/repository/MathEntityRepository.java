package cap.math.repository;

import cap.math.domain.Math;
import cap.math.domain.MathEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MathEntityRepository extends JpaRepository<MathEntity, Long> {
}

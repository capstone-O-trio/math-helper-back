package cap.math.repository;

import cap.math.domain.Math;
import cap.math.domain.MathEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MathEntityRepository extends JpaRepository<MathEntity, Long> {
    List<MathEntity> findALLByMathId(Long mathId);
}

package cap.math.repository;

import cap.math.domain.MathType;
import cap.math.domain.ProbExtractImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProbExtractImageRepository extends JpaRepository<ProbExtractImage, Long> {
    Optional<ProbExtractImage> findByMathId (Long math_id);
}

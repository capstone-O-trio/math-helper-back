package cap.math.repository;

import cap.math.domain.Math;
import cap.math.domain.MathType;
import cap.math.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MathTypeRepository extends JpaRepository<MathType, Long> {
    Boolean existsByTypeName(String type_name);
    Optional<MathType> findByTypeName(String name);
}

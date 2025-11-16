package cap.math.repository;

import cap.math.domain.MathType;
import cap.math.domain.UnitTemplateMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitTemplateMappingRepository extends JpaRepository<UnitTemplateMapping, Long> {
    List<UnitTemplateMapping> findAllByMathType(MathType mathType);
}

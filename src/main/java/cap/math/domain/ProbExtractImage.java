package cap.math.domain;

import cap.math.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProbExtractImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String extractImage;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="math_id")
    private Math math;

}

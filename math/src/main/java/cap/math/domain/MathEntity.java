package cap.math.domain;

import cap.math.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MathEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String image;

    @Column(nullable = false, length = 30)
    private String name;

    private Integer count;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="math_id")
    private Math math;

}

package cap.math.domain;

import cap.math.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Unit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unit_title;

    private Integer grade;

    private Integer semester;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL)
    private List<MathType> mathTypeList=new ArrayList<>();
}

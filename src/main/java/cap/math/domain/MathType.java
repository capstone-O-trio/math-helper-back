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
public class MathType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type_name;

    //@Column(length = 5000)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String  gpt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="unit_id")
    private Unit unit;

    @OneToMany(mappedBy = "mathType", cascade = CascadeType.ALL)
    private List<Math> mathList=new ArrayList<>();

    @OneToMany(mappedBy = "mathType", cascade = CascadeType.ALL)
    private List<UnitTemplateMapping> unitTemplateMappingList=new ArrayList<>();

}

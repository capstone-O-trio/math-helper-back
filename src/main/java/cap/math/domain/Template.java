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
public class Template extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String templateName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String  templateScript;

    private Boolean isPossible;

    private String templateImage;

    //@Column(length = 5000)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String gpt;

    //@Column(length = 5000)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String deploy;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<UnitTemplateMapping> unitTemplateMappingList=new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<TemplateObjMapping> templateObjMappingList=new ArrayList<>();
}

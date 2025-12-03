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
public class Math extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String image;

    private String answer;

    private String problem;

    private String answerScript;

    private String wrongAnswer1;

    private String wrongAnswer2;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isChecked= false;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="math_type_id")
    private MathType mathType;

    @OneToMany(mappedBy = "math", cascade = CascadeType.ALL)
    private List<ProbExtractImage> probExtractImageArrayList=new ArrayList<>();




}

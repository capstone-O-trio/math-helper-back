package cap.math.converter;

import cap.math.domain.Math;
import cap.math.domain.MathEntity;
import cap.math.domain.User;
import cap.math.dto.math.MathRequestDTO;
import cap.math.dto.math.MathResponseDTO;
import cap.math.dto.user.UserResponseDTO;
import cap.math.repository.MathEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MathConverter {
    private final MathEntityRepository mathEntityRepository;

    public static MathResponseDTO.crerateMathDto toCreateMathDto(Math math, List<MathEntity> entities){
        if (entities.size() < 2) {
            throw new RuntimeException("엔티티가 2개 이상이어야 합니다.");
        }

        MathEntity e1 = entities.get(0);
        MathEntity e2 = entities.get(1);

        MathResponseDTO.mathProblemDto problemDto=MathResponseDTO.mathProblemDto.builder()
                .problem(math.getProblem())
                .entity(e1.getEntity())
                .count1(e1.getCount())
                .count2(e2.getCount())
                .answer(math.getAnswer())
                .wrongAnswers(math.getWrongAnswers())
                .build();

        return MathResponseDTO.crerateMathDto.builder()
                .mathId(math.getId())
                .image(math.getImage())
                .mathProblemDto(problemDto)
                .build();
    }

}

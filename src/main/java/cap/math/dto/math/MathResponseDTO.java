package cap.math.dto.math;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MathResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class crerateMathDto{
        private Long mathId;
        private String image;
        private mathProblemDto mathProblemDto;



    }
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class mathProblemDto{
        private String problem;
        private String entity;
        private int count1;
        private int count2;
        private int answer;
        private List<Integer> wrongAnswers;
    }

}

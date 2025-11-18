package cap.math.dto.math;

import jakarta.persistence.criteria.CriteriaBuilder;
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
    public static class crerateMathTypeDto{
        private Long mathId;
        private String image;
        private mathTypeDto mathTypeDto;
    }
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class mathTypeDto{
        private String problem;
        private String type_name;
        private String answer;
        private String extractedImage;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class templateDto{
        private List<singleTemplateDto> templates;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class singleTemplateDto{
        private Long templateId;
        private String templateName;
        private Boolean isPossible;
        private String templateImage;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createParameterDto{
        private String deploy;
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
        private String answer;
        private List<String> wrongAnswers;
    }
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getAnswerDto{
        private Long mathId;
        private String image;
        private String answer;
        private String wrongAnswer1;
        private String wrongAnswer2;
    }

}

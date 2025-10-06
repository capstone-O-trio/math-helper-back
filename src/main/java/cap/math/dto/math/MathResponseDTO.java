package cap.math.dto.math;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MathResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class crerateMathDto{
        Long mathId;
        String image;
        Integer answer;



    }
}

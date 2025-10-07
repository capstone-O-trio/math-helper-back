package cap.math.dto.math;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

public class MathRequestDTO {
    @Getter
    public static class gptDTO{
        MultipartFile quizImage;
    }
}

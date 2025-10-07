package cap.math.service.MathService;

import cap.math.domain.Math;
import cap.math.domain.User;
import cap.math.dto.math.MathResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MathService {
    MathResponseDTO.crerateMathDto createMath(User user, String directory, MultipartFile image);


}

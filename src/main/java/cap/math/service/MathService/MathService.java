package cap.math.service.MathService;

import cap.math.domain.Math;
import cap.math.domain.User;
import org.springframework.web.multipart.MultipartFile;

public interface MathService {
    Math createMath(User user, String directory, MultipartFile image);
    String uploadImage (String directory, MultipartFile image);
}

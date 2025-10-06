package cap.math.service.UserService;

import cap.math.domain.User;
import cap.math.dto.user.UserRequestDTO;

public interface UserService {
    User joinUser(UserRequestDTO.JoinDto request);
}

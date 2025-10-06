package cap.math.service.UserService;

import cap.math.domain.User;
import cap.math.dto.user.UserRequestDTO;
import cap.math.dto.user.UserResponseDTO;

public interface UserService {
    User joinUser(UserRequestDTO.JoinDto request);
    UserResponseDTO.LoginResponse validateUser(UserRequestDTO.loginDto request);
}

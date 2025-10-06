package cap.math.service.UserService;

import cap.math.apiPayload.code.status.ErrorStatus;
import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.config.JwtUtil;
import cap.math.converter.UserConverter;
import cap.math.domain.User;
import cap.math.dto.user.UserRequestDTO;
import cap.math.dto.user.UserResponseDTO;
import cap.math.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    @Override
    @Transactional
    public User joinUser(UserRequestDTO.JoinDto request){

        User user= UserConverter.toUser(request);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDTO.LoginResponse validateUser(UserRequestDTO.loginDto request){
        User user=userRepository.findByName(request.getName())
                .orElseThrow(()->new RuntimeException());

        if(user.getPassword().equals(request.getPassword())){
            String accessToken = jwtUtil.generateAccessToken(user.getName());
            String refreshToken = jwtUtil.generateRefreshToken(user.getName());
            return new UserResponseDTO.LoginResponse(accessToken, refreshToken);
        }
        else{
            throw new TempHandler(ErrorStatus.LOGIN_ERROR_PW);
        }
    }
}

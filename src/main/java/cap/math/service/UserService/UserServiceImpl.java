package cap.math.service.UserService;

import cap.math.converter.UserConverter;
import cap.math.domain.User;
import cap.math.dto.user.UserRequestDTO;
import cap.math.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    @Override
    @Transactional
    public User joinUser(UserRequestDTO.JoinDto request){

        User user= UserConverter.toUser(request);
        return userRepository.save(user);
    }
}

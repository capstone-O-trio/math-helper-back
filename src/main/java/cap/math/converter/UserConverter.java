package cap.math.converter;

import cap.math.domain.User;
import cap.math.dto.user.UserRequestDTO;
import cap.math.dto.user.UserResponseDTO;

import java.time.LocalDateTime;

public class UserConverter {
    public static UserResponseDTO.JoinResultDto toJoinResultDTO(User user){
        return UserResponseDTO.JoinResultDto.builder()
                .userId(user.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static User toUser(UserRequestDTO.JoinDto request){
        return User.builder()
                .name(request.getName())
                .password(request.getPassword())
                .build();
    }
}

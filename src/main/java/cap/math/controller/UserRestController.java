package cap.math.controller;

import cap.math.apiPayload.ApiResponse;
import cap.math.converter.UserConverter;
import cap.math.domain.User;
import cap.math.dto.user.UserRequestDTO;
import cap.math.dto.user.UserResponseDTO;
import cap.math.service.UserService.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "${swagger.tag.auth}")
public class UserRestController {
    private final UserService userService;

    @PostMapping("/signup")
    public ApiResponse<UserResponseDTO.JoinResultDto> signup(@RequestBody @Valid UserRequestDTO.JoinDto request){
        User user=userService.joinUser(request);
        return ApiResponse.onSuccess(UserConverter.toJoinResultDTO(user));
    }
}

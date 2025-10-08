package cap.math.controller;

import cap.math.apiPayload.ApiResponse;
import cap.math.converter.UserConverter;
import cap.math.domain.User;
import cap.math.dto.user.UserRequestDTO;
import cap.math.dto.user.UserResponseDTO;
import cap.math.service.UserService.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary="회원가입 API",
            description="회원가입 API - name이 id입니다.")
    public ApiResponse<UserResponseDTO.JoinResultDto> signup(@RequestBody @Valid UserRequestDTO.JoinDto request){
        User user=userService.joinUser(request);
        return ApiResponse.onSuccess(UserConverter.toJoinResultDTO(user));
    }

    @PostMapping("/login")
    @Operation(summary="로그인 API",
            description="로그인할때 response로 토큰 발급합니다. \n"+
                        "토큰을 복사하여 swagger에 있는 자물쇠 표시에 값을 붙여넣으면 됩니다.")
    public ApiResponse<UserResponseDTO.LoginResponse> login(@RequestBody @Valid UserRequestDTO.loginDto request){
        UserResponseDTO.LoginResponse validated=userService.validateUser(request);
        return ApiResponse.onSuccess(validated);
    }

}

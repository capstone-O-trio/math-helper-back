package cap.math.controller;

import cap.math.apiPayload.ApiResponse;
import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.converter.MathConverter;
import cap.math.domain.Math;
import cap.math.domain.User;
import cap.math.dto.math.MathResponseDTO;
import cap.math.repository.MathRepository;
import cap.math.repository.UserRepository;
import cap.math.service.MathService.MathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static cap.math.apiPayload.code.status.ErrorStatus.MATH_NOT_FOUND;
import static cap.math.apiPayload.code.status.ErrorStatus.USER_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/maths")
@Tag(name = "${swagger.tag.math-quiz}")
public class MathController {
    private final MathService mathService;
    private final UserRepository userRepository;
    private final MathRepository mathRepository;

    @PostMapping(value="/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="사진 촬영 API",
            description="수학문제 사진 업로드 시, GPT로부터 수학문제에 대한 정보를 JSON 데이터로 받아옵니다.")
    public ApiResponse<MathResponseDTO.crerateMathDto> createMath(@AuthenticationPrincipal String userName, @RequestParam("imageFile") MultipartFile image){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        MathResponseDTO.crerateMathDto math= mathService.createMath(user,"image",image);

        return ApiResponse.onSuccess(math);
    }

    @GetMapping(value="/quiz/{mathId}")
    @Operation(summary="문제 풀기 API",
            description="수학문제 ID 입력 시, JSON 데이터을 추출합니다.")
    public ApiResponse<MathResponseDTO.crerateMathDto> getMath(@AuthenticationPrincipal String userName,@PathVariable Long mathId){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        MathResponseDTO.crerateMathDto response=mathService.getMath(mathId);
        return ApiResponse.onSuccess(response);

    }

    @GetMapping(value="/images/{mathId}")
    @Operation(summary="사진 확인 API",
            description="수학문제 ID 입력 시, 해당 문제에 대한 이미지 URL을 추출합니다.")
    public ApiResponse<String> getImage(@AuthenticationPrincipal String userName,@PathVariable Long mathId){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        String response=mathService.getImage(mathId);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping(value="/answers/{mathId}")
    @Operation(summary="정답 확인 API",
            description="수학문제 ID 입력하고 정답을 입력하면, 정오답을 판별합니다.")
    public ApiResponse<String> getAnswer(@AuthenticationPrincipal String userName,@PathVariable Long mathId, @RequestParam Integer answer){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        String response=mathService.getAnswer(mathId, answer);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping(value="/randoms/{mathId}")
    @Operation(summary="정오답 보기 API",
            description="수학문제 ID 입력 시, 정답과 오답 2개를 추출합니다.")
    public ApiResponse<MathResponseDTO.getAnswerDto> getRandom(@AuthenticationPrincipal String userName,@PathVariable Long mathId){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        MathResponseDTO.getAnswerDto response=mathService.getRandom(mathId);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping(value="/new")
    @Operation(summary="새로운 문제 조회 API")
    public ApiResponse<MathResponseDTO.crerateMathDto> getNew(@AuthenticationPrincipal String userName){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        MathResponseDTO.crerateMathDto response=mathService.getNew(user.getId());
        return ApiResponse.onSuccess(response);
    }

}

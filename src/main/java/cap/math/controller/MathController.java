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
    public ApiResponse<MathResponseDTO.crerateMathDto> createMath(@AuthenticationPrincipal String userName, @RequestParam("imageFile") MultipartFile image){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        MathResponseDTO.crerateMathDto math= mathService.createMath(user,"image",image);

        return ApiResponse.onSuccess(math);
    }

    @GetMapping(value="/quiz/{mathId}")
    public ApiResponse<MathResponseDTO.crerateMathDto> getMath(@AuthenticationPrincipal String userName,@PathVariable Long mathId){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        MathResponseDTO.crerateMathDto response=mathService.getMath(mathId);
        return ApiResponse.onSuccess(response);

    }

    @GetMapping(value="/images/{mathId}")
    public ApiResponse<String> getImage(@AuthenticationPrincipal String userName,@PathVariable Long mathId){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        String response=mathService.getImage(mathId);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping(value="/answers/{mathId}")
    public ApiResponse<String> getAnswer(@AuthenticationPrincipal String userName,@PathVariable Long mathId, @RequestParam Integer answer){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        String response=mathService.getAnswer(mathId, answer);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping(value="/randoms/{mathId}")
    public ApiResponse<MathResponseDTO.getAnswerDto> getRandom(@AuthenticationPrincipal String userName,@PathVariable Long mathId){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        MathResponseDTO.getAnswerDto response=mathService.getRandom(mathId);
        return ApiResponse.onSuccess(response);
    }

}

package cap.math.controller;

import cap.math.apiPayload.ApiResponse;
import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.converter.MathConverter;
import cap.math.domain.Math;
import cap.math.domain.User;
import cap.math.dto.math.MathResponseDTO;
import cap.math.repository.UserRepository;
import cap.math.service.MathService.MathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static cap.math.apiPayload.code.status.ErrorStatus.USER_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/maths")
@Tag(name = "${swagger.tag.math-quiz}")
public class MathController {
    private final MathService mathService;
    private final UserRepository userRepository;

    @PostMapping(value="/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MathResponseDTO.crerateMathDto> createMath(@AuthenticationPrincipal String userName, @RequestParam("imageFile") MultipartFile image){
        User user=userRepository.findByName(userName)
                .orElseThrow(()-> new TempHandler(USER_NOT_FOUND));
        MathResponseDTO.crerateMathDto math= mathService.createMath(user,"image",image);

        return ApiResponse.onSuccess(math);
    }

}

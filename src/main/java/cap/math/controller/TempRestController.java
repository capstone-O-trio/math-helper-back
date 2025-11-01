package cap.math.controller;

import cap.math.apiPayload.ApiResponse;
import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.converter.TempConverter;
import cap.math.domain.User;
import cap.math.dto.TempResponse;
import cap.math.dto.math.MathResponseDTO;
import cap.math.service.MathService.MathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static cap.math.apiPayload.code.status.ErrorStatus.USER_NOT_FOUND;

@RestController
@RequestMapping("/temp")
@RequiredArgsConstructor
@Tag(name = "${swagger.tag.test}")
public class TempRestController {
    private final MathService mathService;

    @GetMapping("/test")
    public ApiResponse<TempResponse.TempTestDTO> testAPI(){

        return ApiResponse.onSuccess(TempConverter.toTempTestDTO());
    }
    @PostMapping(value="/gpt", consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="GPT 테스트 API",
            description="프롬포트랑 이미지 업로드하시면 됩니다.")
    public ApiResponse<String> getGpt(@RequestParam("prompt") String prompt,  @RequestParam("imageFile") MultipartFile image ){
        String response = mathService.getSimpleResponse(prompt, "image",image);
        return ApiResponse.onSuccess(response);

    }
}
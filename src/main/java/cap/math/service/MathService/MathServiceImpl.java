package cap.math.service.MathService;


import cap.math.apiPayload.code.status.ErrorStatus;
import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.aws.s3.AmazonS3Manager;

import cap.math.config.GptConfig;
import cap.math.domain.Math;
import cap.math.domain.MathEntity;
import cap.math.domain.User;
import cap.math.dto.math.MathRequestDTO;
import cap.math.dto.math.MathResponseDTO;
import cap.math.repository.MathEntityRepository;
import cap.math.repository.MathRepository;
import cap.math.repository.UuidRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static cap.math.apiPayload.code.status.ErrorStatus.JSON_PARSING_ERROR;
import static cap.math.apiPayload.code.status.ErrorStatus._BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class MathServiceImpl implements MathService {
    private final AmazonS3Manager s3Manager;
    private final MathRepository mathRepository;
    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;
    private final MathEntityRepository mathEntityRepository;

    @Override
    @Transactional
    public MathResponseDTO.crerateMathDto createMath(User user, String directory, MultipartFile image){


        String imageUrl= s3Manager.uploadFile(directory, image);
        String prompt=generatePrompt(imageUrl);
        String response;
        try{
            response = callOpenAI(prompt, 200);
            System.out.print(response);
        } catch (JsonProcessingException e) {
            throw new TempHandler(JSON_PARSING_ERROR);
        }
        ObjectMapper objectMapper=new ObjectMapper();
        MathResponseDTO.mathProblemDto problemDto;
        try{
            problemDto=objectMapper.readValue(extractContent(response), MathResponseDTO.mathProblemDto.class);
        }catch (Exception e) {
            throw new TempHandler(_BAD_REQUEST);
        }


        Math math= Math.builder()
                .image(imageUrl)
                .user(user)
                .problem(problemDto.getProblem())
                .answer(problemDto.getAnswer())
                .wrongAnswers(problemDto.getWrongAnswers())
                .build();
        math=mathRepository.save(math);
        MathEntity entity1 = MathEntity.builder()
                .entity(problemDto.getEntity())
                .count(problemDto.getCount1())
                .math(math)
                .build();
        MathEntity entity2 = MathEntity.builder()
                .entity(problemDto.getEntity())
                .count(problemDto.getCount2())
                .math(math)
                .build();

        mathEntityRepository.save(entity1);
        mathEntityRepository.save(entity2);
        MathResponseDTO.crerateMathDto mathResponse=MathResponseDTO.crerateMathDto.builder()
                .mathId(math.getId())
                .image(imageUrl)
                .mathProblemDto(problemDto)
                .build();

        return mathResponse;
    }

    public String generatePrompt( String imageUrl) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("이미지 주소: ").append(imageUrl).append("\n")
                .append("이 사진은 초등학교 1학년 수학 문제야. 사진 정확히 읽고 정확하게 문제 추출해줘. 아래와 같은 JSON 형식으로 문제를 정리해줘.\n")
                .append("항상 'entity'는 \"apple\"로 고정하고,\n")
                .append("'wrongAnswers'에는 정답과 비슷한 숫자 두 개를 배열로 넣어줘.\n")
                .append("설명 없이 JSON만 출력해줘. 아래는 '2+3은 무엇일까요?'문제였을때 나오는 데이터에 대한 예시를 든거야. 반드시 아래 형식처럼 작성해:\n\n")
                .append("{\n")
                .append("  \"problem\": \"2+3\",\n")
                .append("  \"entity\": \"apple\",\n")
                .append("  \"count1\": 2,\n")
                .append("  \"count2\": 3,\n")
                .append("  \"answer\": 5,\n")
                .append("  \"wrongAnswers\": [4, 6]\n")
                .append("}\n")
                .append("\n")
                .append("Please respond only with the JSON.");

        return prompt.toString();
    }

    public String callOpenAI(String prompt, int maxTokens) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptConfig.getSecretKey());

        // OpenAI 메시지 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", gptConfig.getModel());

        // messages 배열 구성 (system + user 역할)
        requestBody.put("messages", new Object[]{
                // system 역할: 답변 형식, 스타일 설정
                new HashMap<String, String>() {{
                    put("role", "system");
                    put("content", String.format(
                            "You are an assistant that returns only JSON responses. " +
                                    "Do not explain. Only output valid JSON according to the user's prompt."));
                }},
                // user 역할: 실제 prompt 입력
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
        });

        // 기타 설정
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", maxTokens);

        // HTTP 요청 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions", HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String extractContent(String gptResponseJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(gptResponseJson);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // 코드블럭 제거: ```json\n{ ... }\n```
            if (content.startsWith("```json")) {
                content = content.replace("```json", "")
                        .replace("```", "")
                        .trim();
            }

            return content;
        } catch (Exception e) {
            throw new TempHandler(JSON_PARSING_ERROR);
        }
    }



}

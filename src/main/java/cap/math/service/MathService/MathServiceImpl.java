package cap.math.service.MathService;


import cap.math.apiPayload.code.status.ErrorStatus;
import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.aws.s3.AmazonS3Manager;

import cap.math.config.GptConfig;
import cap.math.converter.MathConverter;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cap.math.apiPayload.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
public class MathServiceImpl implements MathService {
    private final AmazonS3Manager s3Manager;
    private final MathRepository mathRepository;
    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;
    private final MathEntityRepository mathEntityRepository;
    private final MathConverter mathConverter;

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
                .isChecked(false)
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
    @Override
    @Transactional
    public MathResponseDTO.crerateMathDto getMath (Long mathId){
        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));
        math.setIsChecked(true);
        List<MathEntity> entities = mathEntityRepository.findALLByMathId(math.getId());
        return MathConverter.toCreateMathDto(math, entities);
    }

    @Override
    @Transactional
    public String getImage (Long mathId){
        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));

        math.setIsChecked(true);
        return math.getImage();
    }
    @Override
    @Transactional
    public String getAnswer (Long mathId, Integer answer){
        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));
        if(Objects.equals(math.getAnswer(), answer)){
            return "정답입니다.";
        }
        else{
            return "오답입니다.";
        }
    }

    @Override
    @Transactional
    public MathResponseDTO.getAnswerDto getRandom(Long mathId){
        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));
        List<Integer> wrongAnswers = math.getWrongAnswers();

        return MathResponseDTO.getAnswerDto.builder()
                .answer(math.getAnswer())
                .mathId(mathId)
                .wrongAnswer1(wrongAnswers.get(0))
                .wrongAnswer2(wrongAnswers.get(1))
                .build();

    }
    @Override
    @Transactional
    public MathResponseDTO.crerateMathDto getNew(Long userId) {
        return mathRepository.findFirstByUserIdAndIsCheckedFalseOrderByCreatedAtDesc(userId)
                .map(math -> {
                    List<MathEntity> entities = mathEntityRepository.findALLByMathId(math.getId());
                    return MathConverter.toCreateMathDto(math, entities);
                })
                .orElse(null);
    }

    public String generatePrompt(String imageUrl) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("이미지 주소: ").append(imageUrl).append("\n")
                .append("이 이미지는 초등학교 1학년 수준의 수학 문제 사진이야.\n")
                .append("1. 먼저 이미지를 자세히 보고, 글자뿐 아니라 그림(사과 개수 등)으로 표현된 수량이 있으면 텍스트 변환해서 '4+5'처럼 이 형식으로 읽어줘.\n")
                .append("2. 문제의 핵심 연산이 덧셈인지 뺄셈인지 판단하고, 문제의 수식 형태(예: 2+3)를 명확히 구성해. 숫자 제발 다시 정확하게 봐. 텍스트 추출 잘해.\n")
                .append("3. JSON은 반드시 아래 예시 형식으로 출력하고, 설명이나 추가 문장은 절대 쓰지 마.\n")
                .append("4. 'entity'는 항상 'apple'로 고정.\n")
                .append("5. 'wrongAnswers'는 정답과 1~2 차이 나는 숫자 두 개로 만들어.\n\n")
                .append("출력 형식 예시:\n")
                .append("{\n")
                .append("  \"problem\": \"2+3\",\n")
                .append("  \"entity\": \"apple\",\n")
                .append("  \"count1\": 2,\n")
                .append("  \"count2\": 3,\n")
                .append("  \"answer\": 5,\n")
                .append("  \"wrongAnswers\": [4, 6]\n")
                .append("}\n\n")
                .append("지금부터 이미지를 분석하고 위 JSON만 정확히 출력해. 그 외 설명은 쓰지 마.");

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

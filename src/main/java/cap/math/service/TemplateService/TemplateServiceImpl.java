package cap.math.service.TemplateService;

import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.config.GptConfig;
import cap.math.domain.*;
import cap.math.domain.Math;
import cap.math.dto.math.MathResponseDTO;
import cap.math.repository.MathRepository;
import cap.math.repository.MathTypeRepository;
import cap.math.repository.TemplateRepository;
import cap.math.repository.UnitTemplateMappingRepository;
import cap.math.service.MathService.MathService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cap.math.apiPayload.code.status.ErrorStatus.*;
import static cap.math.apiPayload.code.status.ErrorStatus._BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    private final MathTypeRepository mathTypeRepository;
    private final UnitTemplateMappingRepository unitTemplateMappingRepository;
    private final MathRepository mathRepository;
    private final TemplateRepository templateRepository;
    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;


    public MathResponseDTO.templateDto getTemplateList(String typeName){
        MathType mathType=mathTypeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new TempHandler(TYPE_NOT_FOUND));

        List<UnitTemplateMapping> unitTemplateMappings=unitTemplateMappingRepository.findAllByMathType(mathType);

        //UnitTemplateMapping에서 id 리스트를 dto에 담기.
        List<Long> templateIds = unitTemplateMappings.stream()
                .map(mapping -> mapping.getTemplate().getId())
                .collect(Collectors.toList());

        // DTO 객체 생성 후 값 세팅
        MathResponseDTO.templateDto dto =  MathResponseDTO.templateDto.builder()
                .templateIds(templateIds)
                .build();

        return dto;


    }

    public String createParameter(Long mathId, Long templateId){

        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));
        String typePrompt=generateTemplatePrompt(mathId,templateId);
        String response;
        try{
            response = callOpenAI(typePrompt, 500);
        } catch (JsonProcessingException e) {
            throw new TempHandler(JSON_PARSING_ERROR);
        }
        return response;

    }

    public String generateTemplatePrompt(Long mathId,Long templateId) {
        StringBuilder prompt = new StringBuilder();
        Math math=mathRepository.findById(mathId).orElseThrow(()->new TempHandler(MATH_NOT_FOUND));
        String mathText=math.getProblem();
        String mathType=math.getMathType().getTypeName();
        Template template= templateRepository.findById(templateId).orElseThrow(()->new TempHandler(TEMPLATE_NOT_FOUND));
        String gpt=template.getGpt();

        prompt.append("문제:").append(mathText).append("\n")
                .append("유형:").append(mathType).append("\n")
                .append("1. 문제 텍스트와 유형은 위와 같아. 이 문제와 유형을 \n")
                .append("2. 문제 텍스트와 문제 이미지를 보고 내가 보낸 유형 리스트 중에 해당하는 유형 이름을 추출해서 typeName에 붙여줘.\n")
                .append("3. JSON은 반드시 아래 예시 형식으로 출력하고, 설명이나 추가 문장은 절대 쓰지 마.\n")
                .append("4. 문제 정답도 구해주고 answer에 붙여줘.\n")
                .append("5. 그리고 이 문제의 학년, 학기, 단원 제목도 알려줘.\n\n")
                .append("6. 템플릿 어떤식으로 해야하는지 보내줄게.\n").append(gpt).append("\n\n")
                .append("출력 형식 예시:\n")
                .append("{\n")
                .append("  \"problem\": \"2+3\",\n")
                .append("  \"typeName\": \"apple\",\n")
                .append("  \"answer\": 5,\n")
                .append("  \"학년\": 3,\n")
                .append("  \"학기\": 2,\n")
                .append("  \"단원\": 1단원,\n")
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
}

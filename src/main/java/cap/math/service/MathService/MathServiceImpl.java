package cap.math.service.MathService;


import cap.math.apiPayload.code.status.ErrorStatus;
import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.aws.s3.AmazonS3Manager;

import cap.math.config.GptConfig;
import cap.math.converter.MathConverter;
import cap.math.domain.*;
import cap.math.domain.Math;
import cap.math.dto.math.MathRequestDTO;
import cap.math.dto.math.MathResponseDTO;
import cap.math.repository.*;
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
import java.util.*;
import java.util.stream.Collectors;

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
    private final MathTypeRepository mathTypeRepository;
    private final ProbExtractImageRepository  probExtractImageRepository;
    private final TemplateRepository templateRepository;

    @Override
    @Transactional
    public MathResponseDTO.crerateMathDto createMath(User user, String directory, MultipartFile image){


        String imageUrl= s3Manager.uploadFile(directory, image);
        String prompt=generatePrompt(imageUrl);
        String response;
        try{
            response = callOpenAI(prompt, imageUrl,200);
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
    public MathResponseDTO.crerateMathTypeDto createMathType(User user, String directory, MultipartFile image){


        String imageUrl= s3Manager.uploadFile(directory, image);
        String typePrompt=generateTypePrompt();
        String response;
        try{
            response = callOpenAIV2(typePrompt, imageUrl, 500);
            System.out.println(response);
        } catch (JsonProcessingException e) {
            throw new TempHandler(JSON_PARSING_ERROR);
        }
        ObjectMapper objectMapper=new ObjectMapper();
        MathResponseDTO.mathTypeDto typeDto;
        try{
            typeDto=objectMapper.readValue(extractContent(response), MathResponseDTO.mathTypeDto.class);
        }catch (Exception e) {
            throw new TempHandler(_BAD_REQUEST);
        }

        MathType mathType=mathTypeRepository.findByTypeName(typeDto.getType_name())
                .orElseThrow(() -> new TempHandler(TYPE_NOT_FOUND));

        Math math= Math.builder()
                .image(imageUrl)
                .user(user)
                .problem(typeDto.getProblem())
                .answer(typeDto.getAnswer())
                .answerScript(typeDto.getAnswerScript())
                .wrongAnswer1(typeDto.getWrongAnswer1())
                .wrongAnswer2(typeDto.getWrongAnswer2())
                .mathType(mathType)
                .isChecked(false)
                .build();
        math=mathRepository.save(math);
        ProbExtractImage extractImage = ProbExtractImage.builder()
                .extractImage(typeDto.getExtractedImage())
                .math(math)
                .build();

        probExtractImageRepository.save(extractImage);
        MathResponseDTO.crerateMathTypeDto typeResponse=MathResponseDTO.crerateMathTypeDto.builder()
                .mathId(math.getId())
                .image(imageUrl)
                .typeScript(mathType.getTypeScript())
                .mathTypeDto(typeDto)
                .build();

        return typeResponse;
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
    public Boolean getAnswer (Long mathId, Integer answer){
        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));
        if(Objects.equals(math.getAnswer(), answer)){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    @Transactional
    public MathResponseDTO.getAnswerDto getRandom(Long mathId){
        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));

        return MathResponseDTO.getAnswerDto.builder()
                .image(math.getImage())
                .answer(math.getAnswer())
                //정답해설 추가
                .answerScript(math.getAnswerScript())
                .wrongAnswer1(math.getWrongAnswer1())
                .wrongAnswer2(math.getWrongAnswer2())
                .mathId(mathId)
                .build();

    }
    @Override
    @Transactional
    public MathResponseDTO.crerateMathTypeDto getNew(Long userId) {
        return mathRepository.findFirstByUserIdAndIsCheckedFalseOrderByCreatedAtDesc(userId)
                .map(math -> {
                    // ProbExtractImage 조회
                    ProbExtractImage extractImage =
                            probExtractImageRepository.findByMathId(math.getId())
                                    .orElse(null);
                    // MathTypeDto 구성
                    MathResponseDTO.mathTypeDto typeDto =
                            MathResponseDTO.mathTypeDto.builder()
                                    .problem(math.getProblem())
                                    .type_name(math.getMathType().getTypeName())
                                    .answer(math.getAnswer())
                                    .wrongAnswer1(math.getWrongAnswer1())
                                    .wrongAnswer2(math.getWrongAnswer2())
                                    .extractedImage(extractImage != null ? extractImage.getExtractImage() : null)
                                    .build();

                    // 최종 반환 DTO 구성
                    return MathResponseDTO.crerateMathTypeDto.builder()
                            .mathId(math.getId())
                            .image(math.getImage())
                            .typeScript(math.getMathType().getTypeScript())
                            .mathTypeDto(typeDto)
                            .build();

                })
                .orElse(null);
    }


    public String generateTypePrompt() {
        StringBuilder prompt = new StringBuilder();
        List<String> gptList = mathTypeRepository.findAll()
                .stream()
                .map(MathType::getGpt) // 각 객체에서 gpt 필드 추출
                .collect(Collectors.toList());
        prompt.append("이 이미지는 초등학교 1학년 수준의 수학 문제 사진이야.\n")
                .append("1. 먼저 이미지를 자세히 보고, 문제를 스크립트 변환해줘. 문제 텍스트는 problem에 붙여줘.\n")
                .append("2. 문제 텍스트와 문제 이미지를 보고 내가 보낸 유형 리스트 중에 해당하는 유형 이름을 추출해서 type_name에 붙여줘.\n")
                .append("3. JSON은 반드시 아래 예시 형식으로 출력하고, 설명이나 추가 문장은 절대 쓰지 마.\n")
                .append("4. 문제 정답도 구해주고 answer에 붙여줘. answer의 타입은 String이야.\n")
                .append("그리고 만약 문제의 답에 선택지가 있다면 wrongAnswer1과 wrongAnswer2에는 선택지에서 정답이 아닌 것을 넣어줘.\n")
                .append("문제의 답에 선택지가 없다면 wrongAnswer1과 wrongAnswer2에는 숫자인 경우에는 +1한 값과 -1한 값을 넣고, 나머지는 너가 판단해서 오답으로 많이 나올 것 같은 걸 적어줘. \n")
                .append("만약 2개 중 하나 고르는 등 wrongAnswer가 2개 이상 나오지 않을 경우 wrongAnswer2는 null로 지정해줘.\n")
                .append("5. 문제 해설도 해줘. 초등학교 1학년이 이해하기 쉽게 어떤 개념이 쓰여 있고, 어떻게 푸는 문제인지 이해하기 쉽고 친근한 말투로 설명해줘. 어떻게 풀어야 정답이 나오는지. 그 설명한 내용은 answerScript에 저장해줘.\n")
                .append("6. 그리고 이 문제에 사진이 있다면 사진 url을 extractedImage에 저장해줘. 만약 없거나 저장할 수 없다면 null로 해.\n\n")
                .append("7. 유형 리스트 보내줄게. 만약 내가 준 유형 리스트 중에서 이 문제의 유형에 해당하는게 없다면 type_name은 그냥 false로 보내고, 있다면 아래와 같이 JSON 형식으로 답변하면돼.\n") .append(gptList).append("\n\n")
                .append("출력 형식 예시:\n")
                .append("{\n")
                .append("  \"problem\": \"2+3\",\n")
                .append("  \"type_name\": \"apple\",\n")
                .append("  \"answer\": \"5\",\n")
                .append("  \"answerScript\": \"이 문제는 더하기 문제야. 숫자 2와 3을 더해서 5가 되는 문제야.\",\n")
                .append("  \"wrongAnswer1\": \"6\",\n")
                .append("  \"wrongAnswer2\": \"4\",\n")
                .append("  \"extractedImage\": string,\n")
                .append("}\n\n")
                .append("지금부터 이미지를 분석하고 위 JSON만 정확히 출력해. 그 외 설명은 쓰지 마.");

        return prompt.toString();
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

    @Override
    @Transactional
    public MathResponseDTO.createParameterDto createParameter(Long mathId, Long templateId){

        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));
        String typePrompt=generateTemplatePrompt(mathId,templateId);
        String imageUrl=math.getImage();
        String response;
        try{
            response = callOpenAIV2(typePrompt, imageUrl, 500);
            System.out.println(response);
        } catch (JsonProcessingException e) {
            throw new TempHandler(JSON_PARSING_ERROR);
        }
        String extractedContent = extractContent(response);
        MathResponseDTO.createParameterDto parameterDto = MathResponseDTO.createParameterDto.builder()
                .deploy(extractedContent)
                .build();


        Template template= templateRepository.findById(templateId)
                .orElseThrow(()-> new TempHandler(TEMPLATE_NOT_FOUND));

        return parameterDto;


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
                .append("1. 문제 텍스트와 유형은 위와 같아. \n")
                .append("1. 문제 텍스트와 유형은 위와 같아. \n")
                .append("2. 템플릿 어떤식으로 해야하는지 보내줄게.\n").append(gpt).append("\n\n")
                .append("3. 만약 entity_type에 값이 없다면 \"grape\"로 해줘. \n")
                .append("지금부터 이미지를 분석하고 너의 답은 정확히 내가 제시한 문자열형식으로만 추출해. { \"entity1\": 6, \"entity2\": 2, \"entity_type\": \"peach\" }");

        return prompt.toString();
    }
    public String callOpenAIV2(String prompt, String imageUrl, int maxTokens) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptConfig.getSecretKey());

        // OpenAI 메시지 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", gptConfig.getModel());

        List<Map<String, Object>> contentList = new ArrayList<>();

        // 1. 텍스트 프롬프트
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        contentList.add(textContent);

        // 2. 이미지 URL (GPT가 실제로 인식할 수 있도록 전달)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");

            Map<String, String> imageUrlMap = new HashMap<>();
            imageUrlMap.put("url", imageUrl);
            imageContent.put("image_url", imageUrlMap);

            contentList.add(imageContent);
        }

        // user 메시지 구성
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", contentList);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", maxTokens);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    public String callOpenAI(String prompt, String imageUrl, int maxTokens) throws JsonProcessingException {
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
    @Override
    @Transactional
    public String getSimpleResponse(String prompt,String directory, MultipartFile image) {
        String imageUrl= s3Manager.uploadFile(directory, image);

        String gptResponse = callOpenAITest(prompt, imageUrl, 500);
        return extractPlainContent(gptResponse);
    }

    private String callOpenAITest(String prompt, String imageUrl, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptConfig.getSecretKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", List.of(
                Map.of("type", "text", "text", prompt),
                Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))
        ));

        requestBody.put("messages", List.of(userMessage));
        requestBody.put("max_tokens", maxTokens);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "GPT 호출 중 오류: " + e.getMessage();
        }
    }

    private String extractPlainContent(String gptResponseJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(gptResponseJson);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return content.trim();
        } catch (Exception e) {
            return "응답 파싱 실패: " + e.getMessage();
        }
    }



}

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
import static cap.math.apiPayload.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    private final MathTypeRepository mathTypeRepository;
    private final UnitTemplateMappingRepository unitTemplateMappingRepository;
    private final MathRepository mathRepository;
    private final TemplateRepository templateRepository;
    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;
    private final MathService mathService;


    public MathResponseDTO.templateDto getTemplateList(String typeName){
        MathType mathType=mathTypeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new TempHandler(TYPE_NOT_FOUND));

        List<UnitTemplateMapping> unitTemplateMappings=unitTemplateMappingRepository.findAllByMathType(mathType);

        List<MathResponseDTO.singleTemplateDto> templateDtos = unitTemplateMappings.stream()
                .map(mapping -> {
                    Template template = mapping.getTemplate();
                    return MathResponseDTO.singleTemplateDto.builder()
                            .templateId(template.getId())
                            .templateName(template.getTemplateName())
                            .templateScript(template.getTemplateScript())
                            .templateImage(template.getTemplateImage())
                            .isPossible(template.getIsPossible())// 필요 시 더 추가
                            .build();
                })
                .collect(Collectors.toList());

        return MathResponseDTO.templateDto.builder()
                .templates(templateDtos)
                .build();


    }




}

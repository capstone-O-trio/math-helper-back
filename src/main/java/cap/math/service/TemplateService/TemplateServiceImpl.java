package cap.math.service.TemplateService;

import cap.math.apiPayload.exception.handler.TempHandler;
import cap.math.domain.Math;
import cap.math.domain.MathType;
import cap.math.domain.UnitTemplateMapping;
import cap.math.dto.math.MathResponseDTO;
import cap.math.repository.MathRepository;
import cap.math.repository.MathTypeRepository;
import cap.math.repository.UnitTemplateMappingRepository;
import cap.math.service.MathService.MathService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static cap.math.apiPayload.code.status.ErrorStatus.MATH_NOT_FOUND;
import static cap.math.apiPayload.code.status.ErrorStatus.TYPE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    private final MathTypeRepository mathTypeRepository;
    private final UnitTemplateMappingRepository unitTemplateMappingRepository;
    private final MathRepository mathRepository;


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

    public MathResponseDTO.createParameterDto createParameter(Long mathId, Long templateId){

        Math math=mathRepository.findById(mathId)
                .orElseThrow(()->new TempHandler(MATH_NOT_FOUND));

    }
}

package cap.math.service.TemplateService;

import cap.math.dto.math.MathResponseDTO;

public interface TemplateService {
    MathResponseDTO.templateDto getTemplateList(String typeName);
}

package cap.math.converter;

import cap.math.domain.Math;
import cap.math.domain.User;
import cap.math.dto.math.MathRequestDTO;
import cap.math.dto.math.MathResponseDTO;
import cap.math.dto.user.UserResponseDTO;

import java.time.LocalDateTime;

public class MathConverter {
    public static MathResponseDTO.crerateMathDto toCrerateMathDto(Math math){
        return MathResponseDTO.crerateMathDto.builder()
                .mathId(math.getId())
                .image(math.getImage())
                .answer(math.getAnswer())
                .build();

    }
}

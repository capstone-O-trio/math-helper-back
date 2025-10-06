package cap.math.converter;

import cap.math.dto.TempResponse;

public class TempConverter {
    public static TempResponse.TempTestDTO toTempTestDTO(){
        return TempResponse.TempTestDTO.builder()
                .testString("This is Test!")
                .build();
    }
}

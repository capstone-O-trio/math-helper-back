package cap.math.dto.user;

import lombok.Getter;

public class UserRequestDTO {
    @Getter
    public static class JoinDto{
        String name;
        String password;
    }
}

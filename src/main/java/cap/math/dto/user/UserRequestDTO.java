package cap.math.dto.user;

import lombok.Getter;

public class UserRequestDTO {
    @Getter
    public static class JoinDto{
        private String name;
        private String password;
    }

    @Getter
    public static class loginDto{
        private String name;
        private String password;
    }
}

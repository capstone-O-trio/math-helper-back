package cap.math.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gpt")
public class GptConfig {

    @Value("${openai.secret-key}")
    private String secretKey;

    @Value("${openai.model}")
    private String model;

}
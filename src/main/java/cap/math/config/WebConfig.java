package cap.math.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 이미지 경로 (CORS credentials 없이 허용)
        registry.addMapping("/api/image/**")
                .allowedOrigins("*")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(false);

        // 그 외 모든 경로는 기존처럼 설정
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://math-helper.site", "https://www.math-helper.site", "https://math-helper-front.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // JSON Message Converter 추가
        converters.add(new MappingJackson2HttpMessageConverter());
    }
}
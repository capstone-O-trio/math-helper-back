package cap.math.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // 이 import가 필요합니다.
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (Stateless에서는 사용하지 않음)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 세션 관리 정책을 STATELESS로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. HTTP 요청에 대한 인가 규칙 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/**", "/users/login/**", "/users/signup/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // 특정 경로는 모두 허용
                        .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
                )

                // 4. 직접 만든 JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 추가/
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://math-helper.site",
                "https://math-helper-front.vercel.app",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    public static class JwtFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;

        public JwtFilter(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();

            return path.startsWith("/swagger-ui")
                    || path.startsWith("/v3/api-docs")
                    || path.startsWith("/swagger-resources")
                    || path.startsWith("/webjars")
                    || path.startsWith("/users/login")
                    || path.startsWith("/users/signup")
                    || path.startsWith("/temp/test")
                    || path.startsWith("/temp/gpt")
                    || path.equals("/");
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            // 헤더에서 "Authorization"을 찾아 Bearer 토큰을 추출
            String token = getTokenFromRequest(request);

            try {
                //토큰 없으면 401
                if (token == null) {
                    setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_MISSING");
                    return;
                }

                //토큰 검증 실패 → 401
                if (!jwtUtil.validateToken(token)) {
                    setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_INVALID");
                    return;
                }

                //토큰 OK → Authentication 설정
                String userId = jwtUtil.getUserIdFromToken(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, null);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);

            } catch (Exception e) {
                setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_ERROR");
            }
        }

        private String getTokenFromRequest(HttpServletRequest request) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return null;
        }
    }

    private static void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = "{ \"status\": " + status + ", \"message\": \"" + message + "\" }";
        response.getWriter().write(body);
    }


}


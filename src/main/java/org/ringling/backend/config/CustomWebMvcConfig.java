package org.ringling.backend.config;

import static org.ringling.backend.common.code.ErrorCode.ACCESS_TOKEN_INVALID;
import static org.ringling.backend.common.code.ErrorCode.ACCESS_TOKEN_NOT_FOUND;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.boot.web.servlet.config.annotation.WebMvcConfigurer;
import froggy.winterframework.context.annotation.Configuration;
import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.context.request.NativeWebRequest;
import froggy.winterframework.web.method.annotation.RequestBodyMethodArgumentResolver;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import java.lang.reflect.Parameter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.auth.jwt.JavaJwtProvider;
import org.ringling.backend.common.exception.CustomException;
import org.ringling.backend.user.service.UserService;

@Configuration
public class CustomWebMvcConfig implements WebMvcConfigurer {

    private final JavaJwtProvider jwtProvider;
    private final UserService userService;

    @Autowired
    public CustomWebMvcConfig(JavaJwtProvider jwtProvider, UserService userService) {
        this.jwtProvider = jwtProvider;
        this.userService = userService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        JwtAuthArgumentResolver resolver = new JwtAuthArgumentResolver(jwtProvider, userService);
        argumentResolvers.add(resolver);

        SnapUrlArgumentResolver snapUrlArgumentResolver = new SnapUrlArgumentResolver();
        argumentResolvers.add(snapUrlArgumentResolver);
    }

    static public class JwtAuthArgumentResolver implements HandlerMethodArgumentResolver {

        private final JavaJwtProvider jwtProvider;
        private final UserService userService;
        private final String BEARER_PREFIX = "Bearer ";

        public JwtAuthArgumentResolver(JavaJwtProvider jwtProvider, UserService userService) {
            this.jwtProvider = jwtProvider;
            this.userService = userService;
        }

        @Override
        public boolean supportsParameter(Parameter parameter) {
            return parameter.isAnnotationPresent(JwtAuth.class);
        }

        @Override
        public Object resolveArgument(
            Parameter parameter, NativeWebRequest webRequest, ModelAndView mavContainer
        ) throws Exception {

            String authorization = webRequest.getNativeRequest(HttpServletRequest.class).getHeader("Authorization");

            JwtAuth annotation = parameter.getAnnotation(JwtAuth.class);
            if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
                if (annotation.required()) {
                    throw new AuthException(ACCESS_TOKEN_NOT_FOUND);
                }
                return null;
            }

            String accessToken = authorization.substring(BEARER_PREFIX.length()).trim();
            if (!jwtProvider.validateToken(accessToken)) {
                throw new AuthException(ACCESS_TOKEN_INVALID);
            }

            DecodedJWT decodedJWT = jwtProvider.parseClaims(accessToken);
            Integer userId = Integer.valueOf(decodedJWT.getSubject());

            return userService.getUserByUserId(userId);
        }
    }

    /**
     * Jackson 바인딩 중 발생한 비즈니스 예외({@link CustomException})가
     * {@code JsonProcessingException}으로 래핑되지 않도록 방지
     *
     * <p>
     * 이 Resolver에서 예외를 언래핑하여 원본 비즈니스 예외 그대로 전달
     * {@link ExceptionHandlerFilter}에서 직접 처리 가능하도록 함
     */
    private static class SnapUrlArgumentResolver extends RequestBodyMethodArgumentResolver {

        @Override
        public boolean supportsParameter(Parameter parameter) {
            return parameter.isAnnotationPresent(ValidSnapUrl.class);
        }

        @Override
        protected  <T> T parseJsonToType(String requestData, Class<T> requiredType) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            try {
                return objectMapper.readValue(requestData, requiredType);
            } catch (JsonProcessingException e) {
                Throwable cause = e.getCause();
                // Jackson Exception으로 래핑 된 예외 객체(e)가 CustomException 인경우
                if (cause instanceof CustomException) {
                    throw (CustomException) cause;
                }
                throw new IllegalArgumentException(
                    "Invalid JSON request body. An error occurred during parsing: \n"
                        + e.getMessage(), e
                );
            }
        }
    }
}
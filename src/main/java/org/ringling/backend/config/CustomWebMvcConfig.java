package org.ringling.backend.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.boot.web.servlet.config.annotation.WebMvcConfigurer;
import froggy.winterframework.context.annotation.Configuration;
import froggy.winterframework.web.method.support.HandlerMethodArgumentResolver;
import java.lang.reflect.Parameter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.ringling.backend.auth.jwt.JavaJwtProvider;
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
    }

    static public class JwtAuthArgumentResolver implements HandlerMethodArgumentResolver {

        private final JavaJwtProvider jwtProvider;
        private final UserService userService;

        public JwtAuthArgumentResolver(JavaJwtProvider jwtProvider, UserService userService) {
            this.jwtProvider = jwtProvider;
            this.userService = userService;
        }

        @Override
        public boolean supportsParameter(Parameter parameter) {
            return parameter.isAnnotationPresent(JwtAuth.class);
        }

        @Override
        public Object resolveArgument(Parameter parameter, HttpServletRequest request) throws Exception {
            String authorization = request.getHeader("Authorization");
            final String BEARER_PREFIX = "Bearer ";
            if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) return null;

            String accessToken = authorization.substring(BEARER_PREFIX.length()).trim();
            if (!jwtProvider.validateToken(accessToken)) {
                return null;
            }

            DecodedJWT decodedJWT = jwtProvider.parseClaims(accessToken);
            Integer userId = Integer.valueOf(decodedJWT.getSubject());

            return userService.getUserByUserId(userId);
        }
    }
}
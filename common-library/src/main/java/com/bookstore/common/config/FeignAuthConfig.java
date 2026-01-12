package com.bookstore.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    String authorization = request.getHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        template.header("Authorization", authorization);
                    }

                    String userId = request.getHeader("X-User-Id");
                    if (userId != null && !userId.isEmpty()) {
                        template.header("X-User-Id", userId);
                    }

                    String userRoles = request.getHeader("X-User-Roles");
                    if (userRoles != null && !userRoles.isEmpty()) {
                        template.header("X-User-Roles", userRoles);
                    }

                    String userEmail = request.getHeader("X-User-Email");
                    if (userEmail != null && !userEmail.isEmpty()) {
                        template.header("X-User-Email", userEmail);
                    }
                }
            }
        };
    }
}

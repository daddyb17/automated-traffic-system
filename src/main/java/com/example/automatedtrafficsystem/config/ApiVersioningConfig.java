package com.example.automatedtrafficsystem.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer, WebMvcRegistrations {

    private final ApiVersionRequestConditionFactory conditionFactory;

    public ApiVersioningConfig() {
        this.conditionFactory = new ApiVersionRequestConditionFactory();
    }

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {
            @Override
            protected RequestCondition<?> getCustomTypeCondition(@NonNull Class<?> handlerType) {
                return conditionFactory.createRequestCondition(handlerType).orElse(null);
            }

            @Override
            protected RequestCondition<?> getCustomMethodCondition(@NonNull Method method) {
                return conditionFactory.createRequestCondition(method).orElse(null);
            }
        };
    }
}

package com.example.automatedtrafficsystem.config;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Optional;

public class ApiVersionRequestConditionFactory {

    @NonNull
    public Optional<RequestCondition<?>> createRequestCondition(@NonNull Class<?> handlerType) {
        return Optional.ofNullable(handlerType.getAnnotation(ApiVersion.class))
                .map(apiVersion -> new ApiVersionRequestCondition(apiVersion.value()));
    }

    @NonNull
    public Optional<RequestCondition<?>> createRequestCondition(@NonNull Method method) {
        return Optional.ofNullable(method.getAnnotation(ApiVersion.class))
                .map(apiVersion -> new ApiVersionRequestCondition(apiVersion.value()));
    }
}

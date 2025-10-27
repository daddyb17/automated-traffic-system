package com.example.automatedtrafficsystem.config;

import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {

    private static final Pattern VERSION_PREFIX_PATTERN = Pattern.compile("v(\\d+)");
    private final int[] apiVersions;

    public ApiVersionRequestCondition(int... apiVersions) {
        this.apiVersions = apiVersions;
    }

    @Override
    @NonNull
    public ApiVersionRequestCondition combine(@NonNull ApiVersionRequestCondition other) {
        return other;
    }

    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        String path = request.getRequestURI();
        Matcher m = VERSION_PREFIX_PATTERN.matcher(path);
        if (m.find()) {
            int version = Integer.parseInt(m.group(1));
            if (Arrays.stream(apiVersions).anyMatch(v -> v == version)) {
                return this;
            }
        }
        return null;
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        return Integer.compare(other.getApiVersions()[0], this.getApiVersions()[0]);
    }

    public int[] getApiVersions() {
        return apiVersions;
    }
}

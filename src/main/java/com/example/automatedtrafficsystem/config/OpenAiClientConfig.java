package com.example.automatedtrafficsystem.config;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiClientConfig {

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.ai.openai.api-key:}')")
    public OpenAiApi openAiApi(@Value("${spring.ai.openai.api-key}") String apiKey) {
        return new OpenAiApi(apiKey);
    }

    @Bean
    @ConditionalOnBean(OpenAiApi.class)
    public ChatClient chatClient(OpenAiApi openAiApi) {
        return new OpenAiChatClient(openAiApi);
    }
}

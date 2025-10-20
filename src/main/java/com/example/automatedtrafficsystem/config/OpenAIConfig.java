package com.example.automatedtrafficsystem.config;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenAIConfig {

    @Value("${spring.ai.openai.api-key:${OPENAI_API_KEY:}}}")
    private String openAiApiKey;

    @Value("gpt-4.1-mini")
    private String model;

    @Bean
    public OpenAiApi openAiApi() {
        if (openAiApiKey == null || openAiApiKey.isEmpty() || openAiApiKey.equals("${OPENAI_API_KEY}")) {
            throw new IllegalStateException("OpenAI API key is not set. Please set the OPENAI_API_KEY environment variable or spring.ai.openai.api-key property.");
        }
        return new OpenAiApi(openAiApiKey);
    }

    @Bean
    public ChatClient chatClient(OpenAiApi openAiApi) {
        // Create a new OpenAiChatClient with the API client
        // The model will be specified in the individual requests
        return new OpenAiChatClient(openAiApi);
    }
    
    @Bean
    public Map<String, Object> openAiChatOptions() {
        // Return a map of options that can be used in the PromptTemplate
        Map<String, Object> options = new HashMap<>();
        options.put("model", model);
        options.put("temperature", 0.7f);
        return options;
    }
}

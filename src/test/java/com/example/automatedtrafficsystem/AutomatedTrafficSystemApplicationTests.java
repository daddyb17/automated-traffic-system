package com.example.automatedtrafficsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.seed-sample-data=false",
        "spring.ai.openai.api-key="
})
class AutomatedTrafficSystemApplicationTests {

    @Test
    void contextLoads() {
    }
}

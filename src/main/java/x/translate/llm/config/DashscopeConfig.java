package x.translate.llm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/22 12:07
 * @since 1.0.0
 */
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "dashscope")
@Configuration
public class DashscopeConfig {

    @Value("${llm-provider.dashscope.api-key}")
    private String apiKey;

    @Value("${llm-provider.dashscope.prompt-template}")
    private String promptTemplate;

    @Value("${llm-provider.dashscope.model}")
    private String model;

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public String getModel() {
        return model;
    }

    public String getApiKey() {
        return apiKey;
    }
}

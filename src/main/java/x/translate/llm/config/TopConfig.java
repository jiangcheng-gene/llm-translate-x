package x.translate.llm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/24 18:00
 * @since 1.0.1
 */
@Configuration
public class TopConfig {

    @Value("${llm-provider.enable}")
    private String enable;

    @Value("${llm-provider.prompt-template}")
    private String promptTemplate;

    public String getEnable() {
        return enable;
    }

    public String getPromptTemplate() {
        return promptTemplate;
    }
}

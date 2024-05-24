package x.translate.llm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/24 18:09
 * @since 1.0.0
 */
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "qianfan")
@Configuration
public class QianfanConfig {

    @Value("${llm-provider.qianfan.access-key}")
    private String accessKey;

    @Value("${llm-provider.qianfan.secret-key}")
    private String secretKey;

    @Value("${llm-provider.qianfan.prompt-template}")
    private String promptTemplate;

    @Value("${llm-provider.qianfan.model}")
    private String model;


    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public String getModel() {
        return model;
    }
}

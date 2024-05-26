package x.translate.llm.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import x.translate.llm.pojo.ProxyConfig;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/26 11:55
 * @since 1.0.2
 */
@Data
@Configuration
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "openai")
@ConfigurationProperties(prefix = "llm-provider.openai")
public class OpenAIConfig {

    private String baseUrl;

    private String apiKey;

    private String promptTemplate;

    private String model;

    private ProxyConfig proxy;

}

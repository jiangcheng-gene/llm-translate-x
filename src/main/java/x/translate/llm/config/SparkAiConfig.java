package x.translate.llm.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/27 13:55
 * @since 1.0.2
 */
@Data
@Configuration
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "sparkai")
@ConfigurationProperties(prefix = "llm-provider.sparkai")
public class SparkAiConfig {

    private String appId;
    private String apiSecret;
    private String apiKey;
    private String model;
    private String promptTemplate;
}

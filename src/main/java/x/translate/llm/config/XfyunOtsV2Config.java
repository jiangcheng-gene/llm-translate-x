package x.translate.llm.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/26 13:55
 * @since 1.0.2
 */
@Data
@Configuration
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "xfyunotsv2")
@ConfigurationProperties(prefix = "llm-provider.xfyunotsv2")
public class XfyunOtsV2Config {

    private String appId;
    private String apiSecret;
    private String apiKey;
}

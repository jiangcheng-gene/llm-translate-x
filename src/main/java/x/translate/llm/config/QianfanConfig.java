package x.translate.llm.config;

import com.baidubce.qianfan.Qianfan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangcong 2024 2024/5/22 12:07
 * @since 1.0.0
 */
@Configuration
public class QianfanConfig {

    @Value("${qianfan.access-key}")
    private String accessKey;

    @Value("${qianfan.secret-key}")
    private String secretKey;

    @Value("${qianfan.prompt-template}")
    private String promptTemplate;

    @Bean
    public Qianfan qianfan() {
        return new Qianfan(accessKey, secretKey);
    }

    @Bean
    public String promptTemplate() {
        return promptTemplate;
    }
}

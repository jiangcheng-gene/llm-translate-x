package x.translate.llm.service.xfyun;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import x.translate.llm.config.SparkAiConfig;
import x.translate.llm.config.TopConfig;
import x.translate.llm.pojo.Result;
import x.translate.llm.service.TranslationService;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/27 14:21
 * @since 1.0.2
 */
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "sparkai")
@Service
public class SparkAiWebSocketClient implements TranslationService {

    private String appId;
    private String apiSecret;
    private String apiKey;
    private String model;
    private String promptTemplate;

    ExecutorService executorService;

    public SparkAiWebSocketClient(SparkAiConfig config, TopConfig topConfig) {
        this.apiSecret = config.getApiSecret();
        this.apiKey = config.getApiKey();
        this.appId = config.getAppId();
        this.model = config.getModel();

        if (StringUtils.hasText(config.getPromptTemplate())) {
            this.promptTemplate = config.getPromptTemplate();
        } else {
            this.promptTemplate = topConfig.getPromptTemplate();
        }

        executorService = Executors.newFixedThreadPool(1);
    }

    public Result translate(String text, String sourceLang, String targetLang) {

        CompletableFuture<String> future = new CompletableFuture<>();

        String prompt = promptTemplate + System.lineSeparator() + text;
        SparkAiTask sparkAiTask = new SparkAiTask(this.appId, this.apiSecret, this.apiKey, this.model, prompt, future);
        executorService.execute(sparkAiTask);

        try {
            String s = future.get(30, TimeUnit.SECONDS);
            return Result.success(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getServiceToken() {
        return "sparkai";
    }

}

package x.translate.llm.service.openai;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;
import static com.theokanning.openai.service.OpenAiService.defaultRetrofit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import x.translate.llm.config.OpenAIConfig;
import x.translate.llm.config.TopConfig;
import x.translate.llm.pojo.ProxyConfig;
import x.translate.llm.pojo.Result;
import x.translate.llm.service.TranslationService;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/26 11:48
 * @since 1.0.2
 */
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "openai")
@Service
public class OpenAiTranslateService implements TranslationService {

    private OpenAiService service;
    private String promptTemplate;

    private String model;


    public OpenAiTranslateService(OpenAIConfig openAIConfig, TopConfig topConfig) {

        ProxyConfig proxyConfig = openAIConfig.getProxy();

        ObjectMapper mapper = defaultObjectMapper();

        OkHttpClient.Builder builder = defaultClient(openAIConfig.getApiKey(), Duration.ZERO).newBuilder();
        if (Objects.nonNull(proxyConfig)
                && StringUtils.hasText(proxyConfig.getType())
                && StringUtils.hasText(proxyConfig.getIp())
                && Objects.nonNull(proxyConfig.getPort())) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getIp(), proxyConfig.getPort()));
            builder.proxy(proxy);
        }
        OkHttpClient client = builder.build();

        Retrofit retrofit;
        if (StringUtils.hasText(openAIConfig.getBaseUrl())) {
            retrofit = new Builder()
                    .baseUrl(openAIConfig.getBaseUrl())
                    .client(client)
                    .addConverterFactory(JacksonConverterFactory.create(mapper))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        } else {
            retrofit = defaultRetrofit(client, mapper);
        }

        OpenAiApi api = retrofit.create(OpenAiApi.class);
        this.service = new OpenAiService(api);

        if (StringUtils.hasText(openAIConfig.getPromptTemplate())) {
            this.promptTemplate = openAIConfig.getPromptTemplate();
        } else {
            this.promptTemplate = topConfig.getPromptTemplate();
        }

        this.model = openAIConfig.getModel();
    }

    @Override
    public Result translate(String text, String sourceLang, String targetLang) {

        String prompt = promptTemplate + System.lineSeparator() + text;

        CompletionRequest completionRequest = CompletionRequest.builder()
                .model(model)
                .prompt(prompt)
                .user("llm-translate-x")
                .n(1)
                .maxTokens(4096)
                .build();
        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
        if (CollectionUtils.isEmpty(choices)) {
            return Result.ERR;
        }

        CompletionChoice completionChoice = choices.get(0);
        String content = completionChoice.getText();
        return Result.success(content);
    }

    @Override
    public String getServiceToken() {
        return "openai";
    }

}

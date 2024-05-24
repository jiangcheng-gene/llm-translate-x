package x.translate.llm.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationOutput.Choice;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import x.translate.llm.config.DashscopeConfig;
import x.translate.llm.config.TopConfig;
import x.translate.llm.pojo.Result;

/**
 * 阿里翻译
 *
 * @author jiancheng.gene@proton.me 2024 2024/5/22 10:31
 * @since 1.0.0
 */
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "dashscope")
@Service
public class AliTranslationService implements TranslationService {

    private String model;
    private String apiKey;
    private String promptTemplate;


    public AliTranslationService(DashscopeConfig dashscopeConfig, TopConfig topConfig) {

        if (StringUtils.hasText(dashscopeConfig.getPromptTemplate()) || StringUtils.hasText(
                topConfig.getPromptTemplate())) {

            this.model = dashscopeConfig.getModel();
            this.apiKey = dashscopeConfig.getApiKey();

            if (StringUtils.hasText(dashscopeConfig.getPromptTemplate())) {
                this.promptTemplate = dashscopeConfig.getPromptTemplate();
            } else {
                this.promptTemplate = topConfig.getPromptTemplate();
            }
        } else {
            throw new IllegalArgumentException("please config prompt template!");
        }
    }


    /**
     * 利用百度的大模型对话服务完成翻译
     *
     * @param text       需要翻译的文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @return 翻译后的文本
     */
    public Result translate(String text, String sourceLang, String targetLang) {

        String prompt = promptTemplate + System.lineSeparator() + text;

        Generation gen = new Generation();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .build();

        try {
            GenerationResult call = gen.call(param);
            GenerationOutput output = call.getOutput();
            if (Objects.nonNull(output)) {
                List<Choice> choices = output.getChoices();
                if (CollectionUtils.isEmpty(choices)) {
                    return Result.error(call);
                }

                if (choices.size() == 1) {
                    return Result.success(choices.get(0).getMessage());
                }

                String collect = choices.stream().map(c -> c.getMessage().getContent()).collect(Collectors.joining(""));
                return Result.success(collect);
            } else {
                return Result.error(call);
            }
        } catch (Throwable e) {
            return Result.error(e.getMessage());
        }
    }

    @Override
    public String getServiceToken() {
        return "dashscope";
    }

}

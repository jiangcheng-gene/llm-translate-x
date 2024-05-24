package x.translate.llm.service;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.chat.ChatResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import x.translate.llm.config.QianfanConfig;
import x.translate.llm.config.TopConfig;
import x.translate.llm.pojo.Result;

/**
 * 百度翻译
 *
 * @author jiancheng.gene@proton.me 2024 2024/5/22 10:31
 * @since 1.0.0
 */
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "qianfan")
@Service
public class BaiduTranslationService implements TranslationService {

    private String model;

    private Qianfan qianfan;

    private String promptTemplate;


    public BaiduTranslationService(QianfanConfig qianfanConfig, TopConfig topConfig) {
        this.qianfan = new Qianfan(qianfanConfig.getAccessKey(), qianfanConfig.getSecretKey());
        this.model = qianfanConfig.getModel();

        if (StringUtils.hasText(qianfanConfig.getPromptTemplate())) {
            this.promptTemplate = qianfanConfig.getPromptTemplate();
        } else {

            if (StringUtils.hasText(topConfig.getPromptTemplate())) {
                this.promptTemplate = topConfig.getPromptTemplate();
            } else {
                throw new IllegalArgumentException("please config prompt template!");
            }
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
        ChatResponse response = qianfan.chatCompletion()
                .model(model)
                .addMessage("user", prompt)
                .execute();
        if (response.getFlag() == null) {
            return Result.success(response.getResult());
        }
        return Result.error(response.getResult());
    }

    @Override
    public String getServiceToken() {
        return "qianfan";
    }

}

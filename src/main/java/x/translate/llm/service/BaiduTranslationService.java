package x.translate.llm.service;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.chat.ChatResponse;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import x.translate.llm.pojo.Result;

/**
 * 百度翻译
 *
 * @author wangcong 2024 2024/5/22 10:31
 * @since 1.0.0
 */
@Service
public class BaiduTranslationService {

    private static final Logger logger = LoggerFactory.getLogger(BaiduTranslationService.class);


    @Value("${qianfan.model}")
    private String model;

    @Autowired
    private Qianfan qianfan;

    @Autowired
    private String promptTemplate;


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

        Instant start = Instant.now();
        ChatResponse response = qianfan.chatCompletion().model(model).addMessage("user", prompt).execute();
        Instant end = Instant.now();

        Duration duration = Duration.between(start, end);

        long l = duration.toMillis();

        // 获取Duration的各种表示形式

        if (response.getFlag() == null) {
            logger.info("translate success. cost {} mils", l);
            return Result.success(response.getResult());
        }

        logger.error("translate error. error message:{},  cost {} mils", response.getResult(), l);
        return Result.error(response.getResult());
    }

}

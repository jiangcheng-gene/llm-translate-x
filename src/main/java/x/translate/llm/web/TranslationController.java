package x.translate.llm.web;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import x.translate.llm.pojo.Result;
import x.translate.llm.pojo.TranslationRequest;
import x.translate.llm.service.TranslationService;


/**
 * @author jiancheng.gene@proton.me 2024 2024/5/22 10:30
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class TranslationController {

    private static final Logger logger = LoggerFactory.getLogger(TranslationController.class);

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping("/translate")
    public ResponseEntity<Map<String, Object>> translate(@RequestBody TranslationRequest request) {

        // 调用百度API进行翻译
        Instant start = Instant.now();
        Result result = translationService.translate(request.getText(),
                request.getSourceLang(),
                request.getTargetLang());
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long l = duration.toMillis();
        if (result.isOk()) {
            logger.info("translate success. cost {} mils", l);
        } else {
            logger.error("translate error. error message:{},  cost {} mils", result.getData(), l);
        }

        // 构造响应
        Map<String, Object> response = new HashMap<>();
        // 实测可以不用返回这个属性
        // response.put("alternatives", new String[]{"示例备选译文1", "示例备选译文2"});
        response.put("code", result.getCode());
        response.put("data", result.getData());
        response.put("id", System.currentTimeMillis());
        response.put("method", "Free");
        response.put("source_lang", request.getSourceLang());
        response.put("target_lang", request.getTargetLang());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
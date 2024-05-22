package x.translate.llm.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import x.translate.llm.pojo.Result;
import x.translate.llm.pojo.TranslationRequest;
import x.translate.llm.service.BaiduTranslationService;


/**
 * @author wangcong 2024 2024/5/22 10:30
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class TranslationController {

    private final BaiduTranslationService baiduTranslationService;

    public TranslationController(BaiduTranslationService baiduTranslationService) {
        this.baiduTranslationService = baiduTranslationService;
    }

    @PostMapping("/translate")
    public ResponseEntity<Map<String, Object>> translate(@RequestBody TranslationRequest request) {

        // 调用百度API进行翻译
        Result result = baiduTranslationService.translate(request.getText(), request.getSourceLang(),
                request.getTargetLang());

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
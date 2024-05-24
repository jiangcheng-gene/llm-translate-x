package x.translate.llm.service;

import x.translate.llm.pojo.Result;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/24 17:19
 * @since 1.0.1
 */
public interface TranslationService {


    /**
     * 翻译服务
     *
     * @param text       被翻译的文档
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @return 翻译结果
     */
    Result translate(String text, String sourceLang, String targetLang);

    /**
     * @return 服务标识
     */
    String getServiceToken();

}

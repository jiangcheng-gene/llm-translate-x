package x.translate.llm.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/22 12:40
 * @since 1.0.0
 */
public class TranslationRequest {

    private String text;

    @JsonProperty("target_lang")
    private String targetLang;

    @JsonProperty("source_lang")
    private String sourceLang;

    // Getters and setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

}

package x.translate.llm.service.xfyun;

import java.util.Arrays;
import lombok.Getter;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/27 15:25
 * @since 1.0.2
 */
@Getter
public enum SparkAiMetadata {

    Little("sparklite", "https://spark-api.xf-yun.com/v1.1/chat", "general"),
    SparkV2("sparklite", "https://spark-api.xf-yun.com/v2.1/chat", "generalv2"),
    SparkPro("sparkpro", "https://spark-api.xf-yun.com/v3.1/chat", "generalv3"),
    Spark35("spark35", "https://spark-api.xf-yun.com/v3.5/chat", "generalv3.5");

    private String code;
    private String url;
    private String domain;

    SparkAiMetadata(String code, String url, String domain) {
        this.code = code;
        this.url = url;
        this.domain = domain;
    }

    public static SparkAiMetadata fromCode(String code) {
        return Arrays.stream(SparkAiMetadata.values()).filter(el -> el.getCode().equals(code)).findFirst().get();
    }


}

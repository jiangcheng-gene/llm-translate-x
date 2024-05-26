package x.translate.llm.pojo;

import lombok.Data;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/26 11:58
 * @since 1.0.2
 */
@Data
public class ProxyConfig {

    private String type;
    private String ip;
    private Integer port;

}

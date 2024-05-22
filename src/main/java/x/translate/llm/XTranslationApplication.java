package x.translate.llm;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author wangcong 2024 2024/5/22 12:35
 * @since 1.0.0
 */
@SpringBootApplication
public class XTranslationApplication {

    private static final Logger logger = LoggerFactory.getLogger(XTranslationApplication.class);

    public static void main(String[] args) {

        ConfigurableApplicationContext run = SpringApplication.run(XTranslationApplication.class, args);

        String property = run.getEnvironment().getProperty("server.port");

        logger.info("translate server running on: http://localhost:{}/api/translate", property);
    }

}

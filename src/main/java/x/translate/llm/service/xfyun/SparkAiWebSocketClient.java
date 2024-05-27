package x.translate.llm.service.xfyun;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.HttpUrl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import x.translate.llm.config.SparkAiConfig;
import x.translate.llm.config.TopConfig;
import x.translate.llm.pojo.Result;
import x.translate.llm.service.TranslationService;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/27 14:21
 * @since 1.0.2
 */
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "sparkai")
@Service
public class SparkAiWebSocketClient implements TranslationService {

    public static final Logger logger = LoggerFactory.getLogger(SparkAiWebSocketClient.class);

    public final Map<String, String> mapToUrl = new HashMap<>();
    private String appId;
    private String apiSecret;
    private String apiKey;
    private String model;
    private String promptTemplate;

    public SparkAiWebSocketClient(SparkAiConfig config, TopConfig topConfig) {
        this.apiSecret = config.getApiSecret();
        this.apiKey = config.getApiKey();
        this.appId = config.getAppId();
        this.model = config.getModel();

        if (StringUtils.hasText(config.getPromptTemplate())) {
            this.promptTemplate = config.getPromptTemplate();
        } else {
            this.promptTemplate = topConfig.getPromptTemplate();
        }
    }


    private WebSocketClient webSocketClient;
    private CompletableFuture<String> responseFuture;
    private SparkAiMetadata sparkAiMetadata;

    @PostConstruct
    public void init() {
        sparkAiMetadata = SparkAiMetadata.fromCode(this.model);
        try {
            String authUrl = getAuthUrl(sparkAiMetadata.getUrl(), apiKey, apiSecret);
            String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
            URI uri = new URI(url);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.info("WebSocket connection established");
                }

                @Override
                public void onMessage(String message) {
                    responseFuture.complete(message);
                    logger.info("Received message: {}", message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.info("WebSocket connection closed: {} - {}", code, reason);
                }

                @Override
                public void onError(Exception ex) {
                    responseFuture.completeExceptionally(ex);
                    logger.error("WebSocket error", ex);
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            logger.error("WebSocket connection failed", e);
        }
    }


    // 鉴权方法
    public String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath()))
                .newBuilder().//
                        addQueryParameter("authorization",
                        Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                        addQueryParameter("date", date).//
                        addQueryParameter("host", url.getHost()).//
                        build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }


    public Result translate(String text, String sourceLang, String targetLang) {

        responseFuture = new CompletableFuture<>();
        try {
            if (webSocketClient != null && webSocketClient.isOpen()) {

                String prompt = promptTemplate + System.lineSeparator() + text;

                JsonObject header = new JsonObject();
                header.addProperty("app_id", this.appId);

                JsonObject parameter = new JsonObject();
                JsonObject chat = new JsonObject();
                chat.addProperty("domain", sparkAiMetadata.getDomain());
                chat.addProperty("temperature", 0.2);
                chat.addProperty("max_tokens", 4096);
                parameter.add("chat", chat);

                JsonObject el = new JsonObject();
                el.addProperty("role", "user");
                el.addProperty("content", prompt);

                JsonArray textEl = new JsonArray();
                textEl.add(el);

                JsonObject message = new JsonObject();
                message.add("text", textEl);

                JsonObject payload = new JsonObject();
                payload.add("message", message);

                JsonObject requestBody = new JsonObject();
                requestBody.add("header", header);
                requestBody.add("parameter", parameter);
                requestBody.add("payload", payload);

                webSocketClient.send(requestBody.toString());
            } else {
                responseFuture.completeExceptionally(new IllegalStateException("WebSocket session is not open."));
            }

            String s = responseFuture.get();
            return Result.success(s);
        } catch (Exception e) {
            logger.error("error", e);
            responseFuture.completeExceptionally(e);
            return Result.error(e.getMessage());
        }
    }

    @Override
    public String getServiceToken() {
        return "sparkai";
    }

}

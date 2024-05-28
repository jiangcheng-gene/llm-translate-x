package x.translate.llm.service.xfyun;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.HttpUrl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/28 18:49
 * @since 1.0.2
 */
public class SparkAiTask implements Runnable {

    public static final Logger logger = LoggerFactory.getLogger(SparkAiTask.class);

    private String appId;
    private String apiSecret;
    private String apiKey;
    private String model;
    private String prompt;
    private CompletableFuture<String> responseFuture;


    // ws 消息缓存
    private StringBuilder sb = new StringBuilder();

    private SparkAiMetadata sparkAiMetadata;

    public SparkAiTask(String appId, String apiSecret, String apiKey, String model, String prompt,
            CompletableFuture<String> responseFuture) {
        this.appId = appId;
        this.apiSecret = apiSecret;
        this.apiKey = apiKey;
        this.model = model;
        this.prompt = prompt;
        this.responseFuture = responseFuture;
    }

    public String getAuthUrl(String hostUrl) throws Exception {
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
        return httpUrl.toString();
    }


    @Override
    public void run() {

        sparkAiMetadata = SparkAiMetadata.fromCode(this.model);
        try {
            String authUrl = getAuthUrl(sparkAiMetadata.getUrl());
            String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
            URI uri = new URI(url);
            WebSocketClient webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.info("WebSocket connection established");
                }

                @Override
                public void onMessage(String message) {

                    JsonParse myJsonParse = new Gson().fromJson(message, JsonParse.class);

                    if (myJsonParse.header.code != 0) {
                        System.out.println("发生错误，错误码为：" + myJsonParse.header.code);
                        System.out.println("发生错误，错误信息为：" + myJsonParse.header.message);
                        System.out.println("本次请求的sid为：" + myJsonParse.header.sid);
                        close(1000, "");
                    }

                    List<Text> textList = myJsonParse.payload.choices.text;
                    for (Text temp : textList) {
                        sb.append(temp.content);
                    }

                    if (myJsonParse.header.status == 2) {
                        // 可以关闭连接，释放资源
                        responseFuture.complete(sb.toString());
                        close(1000, "Closing");
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.info("WebSocket connection closed: {} - {} - {}", code, reason, remote);
                }

                @Override
                public void onError(Exception ex) {
                    responseFuture.completeExceptionally(ex);
                    logger.error("WebSocket error", ex);
                }
            };

            webSocketClient.connect();
            int max = 3;
            while (!webSocketClient.isOpen() && --max >= 0) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!webSocketClient.isOpen()) {
                logger.error("can't open wss connection in 3 seconds.");
                throw new RuntimeException("can't open wss connection in 3 seconds.");
            }
            String s = buildSendMessage();
            webSocketClient.send(s);
        } catch (Exception e) {
            logger.error("WebSocket connection failed", e);
        }
    }


    private String buildSendMessage() {
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

        return requestBody.toString();
    }


    class JsonParse {

        Header header;
        Payload payload;
    }

    class Header {

        int code;
        int status;
        String sid;
        String message;
    }

    class Payload {

        Choices choices;
    }

    class Choices {

        List<Text> text;
    }

    class Text {

        String role;
        String content;
    }
}

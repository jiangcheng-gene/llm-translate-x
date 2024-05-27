package x.translate.llm.service.xfyun;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import x.translate.llm.config.XfyunOtsV2Config;
import x.translate.llm.pojo.Result;
import x.translate.llm.service.TranslationService;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/26 13:58
 * @since 1.0.2
 */
@ConditionalOnProperty(prefix = "llm-provider", name = "enable", havingValue = "xfyunotsv2")
@Service
public class Xfyunotsv2TranslationService implements TranslationService {

    public static final Logger logger = LoggerFactory.getLogger(Xfyunotsv2TranslationService.class);


    private String apiUrl = "https://ntrans.xfyun.cn/v2/ots";
    private String appId;
    private String apiSecret;
    private String apiKey;

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Gson gson = new Gson();

    public Xfyunotsv2TranslationService(XfyunOtsV2Config config) {
        this.apiSecret = config.getApiSecret();
        this.apiKey = config.getApiKey();
        this.appId = config.getAppId();
    }


    @Override
    public Result translate(String text, String sourceLang, String targetLang) {
        try {

            String body = buildHttpBody(text, sourceLang, targetLang);
            HttpHeaders httpHeaders = buildHttpHeader(body);

            HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                return Result.error(response.getBody());
            }

            String respText = response.getBody();
            Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> resultData = gson.fromJson(respText, mapType);

            int code = ((Double) resultData.get("code")).intValue();
            if (code != 0) {
                return Result.error(resultData.get("message"));
            }

            Map<String, Object> data = (Map<String, Object>) resultData.get("data");
            Map<String, Object> result = (Map<String, Object>) data.get("result");
            Map<String, Object> transResult = (Map<String, Object>) result.get("trans_result");
            Object dst = transResult.get("dst");
            return Result.success(dst);

        } catch (Throwable ex) {
            logger.error("error", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * 组装http请求头
     */
    public HttpHeaders buildHttpHeader(String body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        URL url = new URL(apiUrl);

        //时间戳
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateD = new Date();
        String date = format.format(dateD);

        //对body进行sha256签名,生成digest头部，POST请求必须对body验证
        String digestBase64 = "SHA-256=" + signBody(body);

        //hmacsha256加密原始字符串
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
                append("date: ").append(date).append("\n").//
                append("POST ").append(url.getPath()).append(" HTTP/1.1").append("\n").//
                append("digest: ").append(digestBase64);
        String sha = hmacsign(builder.toString(), apiSecret);

        //组装authorization
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line digest", sha);
        //System.out.println("【OTS WebAPI authorization】\n" + authorization);

        headers.add("Authorization", authorization);
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json,version=1.0");
        headers.add("Host", url.getHost());
        headers.add("Date", date);
        headers.add("Digest", digestBase64);
        return headers;
    }


    /**
     * 组装http请求体
     */
    public String buildHttpBody(String text, String sourceLang, String targetLang) throws Exception {
        JsonObject body = new JsonObject();
        JsonObject business = new JsonObject();
        JsonObject common = new JsonObject();
        JsonObject data = new JsonObject();
        //填充common
        common.addProperty("app_id", appId);
        //填充business
        business.addProperty("from", sourceLang.toLowerCase());
        business.addProperty("to", targetLang.toLowerCase());
        //填充data
        byte[] textByte = text.getBytes("UTF-8");
        String textBase64 = Base64.getEncoder().encodeToString(textByte);
        //System.out.println("【OTS WebAPI textBase64编码后长度：】\n" + textBase64.length());
        data.addProperty("text", textBase64);
        //填充body
        body.add("common", common);
        body.add("business", business);
        body.add("data", data);
        return body.toString();
    }


    /**
     * 对body进行SHA-256加密
     */
    private String signBody(String body) throws Exception {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(body.getBytes("UTF-8"));
            encodestr = Base64.getEncoder().encodeToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    /**
     * hmacsha256加密
     */
    private static String hmacsign(String signature, String apiSecret) throws Exception {
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(signature.getBytes(charset));
        return Base64.getEncoder().encodeToString(hexDigits);
    }

    public static class ResponseData {

        private int code;
        private String message;
        private String sid;
        private Object data;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return this.message;
        }

        public String getSid() {
            return sid;
        }

        public Object getData() {
            return data;
        }
    }


    @Override
    public String getServiceToken() {
        return "xfyunotsv2";
    }
}

package x.translate.llm.service.openai;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * @author jiancheng.gene@proton.me 2024 2024/5/26 12:31
 * @since 1.0.2
 */
final class JacksonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final ObjectReader adapter;

    JacksonResponseBodyConverter(ObjectReader adapter) {
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            return adapter.readValue(value.charStream());
        } finally {
            value.close();
        }
    }
}

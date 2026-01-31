package top.flobby.admin.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

/**
 * Jackson XSS 防护配置
 * 在 JSON 反序列化时对字符串进行 XSS 转义
 */
@Configuration
public class JacksonXssConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer xssCustomizer() {
        return builder -> {
            SimpleModule xssModule = new SimpleModule("XssModule");
            xssModule.addDeserializer(String.class, new XssStringDeserializer());
            builder.modules(xssModule);
        };
    }

    /**
     * XSS 字符串反序列化器
     */
    public static class XssStringDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.isEmpty()) {
                return value;
            }
            // 对 HTML 特殊字符进行转义
            return HtmlUtils.htmlEscape(value);
        }
    }
}

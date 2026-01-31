package top.flobby.admin.common.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XSS 请求包装器
 * 对请求参数和请求头进行 HTML 转义
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return escape(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        return Arrays.stream(values)
                .map(this::escape)
                .toArray(String[]::new);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = super.getParameterMap();
        Map<String, String[]> escaped = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String[] values = entry.getValue();
            if (values == null) {
                escaped.put(entry.getKey(), null);
                continue;
            }
            String[] escapedValues = Arrays.stream(values)
                    .map(this::escape)
                    .toArray(String[]::new);
            escaped.put(entry.getKey(), escapedValues);
        }
        return escaped;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return escape(value);
    }

    private String escape(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return HtmlUtils.htmlEscape(value);
    }
}

package top.flobby.admin.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Locale;

/**
 * XSS 过滤器
 * 对请求参数和请求头进行 XSS 转义处理
 */
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // 跳过文件上传请求
        if (isMultipart(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        XssRequestWrapper wrapper = new XssRequestWrapper(httpRequest);
        chain.doFilter(wrapper, response);
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }
}

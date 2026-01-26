package top.flobby.admin.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.flobby.admin.common.annotation.OperLog;

import java.util.Arrays;

/**
 * 操作日志 AOP 切面
 */
@Aspect
@Component
public class OperLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperLogAspect.class);

    private final ObjectMapper objectMapper;

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    public OperLogAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 前置通知：记录开始时间
     */
    @Before("@annotation(operLog)")
    public void doBefore(JoinPoint joinPoint, OperLog operLog) {
        startTime.set(System.currentTimeMillis());
    }

    /**
     * 后置通知：记录操作日志
     */
    @AfterReturning(pointcut = "@annotation(operLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, OperLog operLog, Object result) {
        handleLog(joinPoint, operLog, null, result);
    }

    /**
     * 异常通知：记录异常日志
     */
    @AfterThrowing(pointcut = "@annotation(operLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, OperLog operLog, Exception e) {
        handleLog(joinPoint, operLog, e, null);
    }

    /**
     * 处理日志
     */
    private void handleLog(JoinPoint joinPoint, OperLog operLog, Exception e, Object result) {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return;
            }

            // 计算耗时
            long costTime = System.currentTimeMillis() - startTime.get();
            startTime.remove();

            // 构建日志信息
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("\n========== 操作日志 ==========\n");
            logBuilder.append("模块: ").append(operLog.title()).append("\n");
            logBuilder.append("业务类型: ").append(operLog.businessType()).append("\n");
            logBuilder.append("请求方法: ").append(joinPoint.getSignature().toShortString()).append("\n");
            logBuilder.append("请求URL: ").append(request.getRequestURI()).append("\n");
            logBuilder.append("请求方式: ").append(request.getMethod()).append("\n");
            logBuilder.append("请求IP: ").append(getIpAddress(request)).append("\n");

            // 保存请求参数
            if (operLog.saveRequestData()) {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    logBuilder.append("请求参数: ").append(Arrays.toString(args)).append("\n");
                }
            }

            // 保存响应结果
            if (operLog.saveResponseData() && result != null) {
                String jsonResult = objectMapper.writeValueAsString(result);
                if (jsonResult.length() > 500) {
                    jsonResult = jsonResult.substring(0, 500) + "...";
                }
                logBuilder.append("响应结果: ").append(jsonResult).append("\n");
            }

            // 异常信息
            if (e != null) {
                logBuilder.append("异常信息: ").append(e.getMessage()).append("\n");
                logBuilder.append("状态: 失败\n");
            } else {
                logBuilder.append("状态: 成功\n");
            }

            logBuilder.append("耗时: ").append(costTime).append("ms\n");
            logBuilder.append("==============================");

            log.info(logBuilder.toString());

            // TODO: 异步保存到数据库

        } catch (Exception ex) {
            log.error("操作日志记录失败", ex);
        }
    }

    /**
     * 获取请求对象
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

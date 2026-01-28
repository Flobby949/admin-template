package top.flobby.admin.monitor.infrastructure.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.flobby.admin.common.annotation.OperLog;
import top.flobby.admin.monitor.application.OperationLogService;
import top.flobby.admin.monitor.domain.entity.OperationLog;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志记录切面
 * <p>
 * 职责:
 * - 拦截带有@OperLog注解的方法
 * - 采集操作信息(请求参数、响应结果、异常信息等)
 * - 调用OperationLogService保存到数据库
 * <p>
 * 注意:
 * - 使用REQUIRES_NEW事务,避免影响主业务
 * - 记录失败不阻断主业务
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    /**
     * 前置通知:记录开始时间
     */
    @Before("@annotation(operLog)")
    public void doBefore(JoinPoint joinPoint, OperLog operLog) {
        startTime.set(System.currentTimeMillis());
    }

    /**
     * 后置通知:记录操作日志
     */
    @AfterReturning(pointcut = "@annotation(operLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, OperLog operLog, Object result) {
        handleLog(joinPoint, operLog, null, result);
    }

    /**
     * 异常通知:记录异常日志
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

            // 创建操作日志实体
            OperationLog log = new OperationLog();
            log.setTitle(operLog.title());
            log.setBusinessType(operLog.businessType().ordinal());
            log.setMethod(joinPoint.getSignature().toShortString());
            log.setRequestMethod(request.getMethod());
            // 1-后台用户
            log.setOperatorType(1);
            log.setOperUrl(request.getRequestURI());
            log.setOperIp(getIpAddress(request));
            log.setOperTime(LocalDateTime.now());
            log.setCostTime(costTime);

            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                log.setOperName(authentication.getName());
            }

            // 保存请求参数
            if (operLog.saveRequestData()) {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    try {
                        String params = objectMapper.writeValueAsString(args);
                        log.setOperParam(params);
                    } catch (Exception ex) {
                        log.setOperParam(Arrays.toString(args));
                    }
                }
            }

            // 保存响应结果
            if (operLog.saveResponseData() && result != null) {
                try {
                    String jsonResult = objectMapper.writeValueAsString(result);
                    log.setJsonResult(jsonResult);
                } catch (Exception ex) {
                    log.setJsonResult(result.toString());
                }
            }

            // 异常信息
            if (e != null) {
                // 失败
                log.setStatus(0);
                log.setErrorMsg(e.getMessage());
            } else {
                // 成功
                log.setStatus(1);
            }

            // 保存到数据库(使用REQUIRES_NEW事务)
            operationLogService.recordOperationLog(log);

        } catch (Exception ex) {
            // 记录日志失败不影响主业务
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
     * 获取客户端IP地址
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

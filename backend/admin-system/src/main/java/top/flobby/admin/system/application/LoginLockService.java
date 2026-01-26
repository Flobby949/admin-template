package top.flobby.admin.system.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.flobby.admin.common.exception.BusinessException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_KEY_PREFIX = "auth:login:lock:";
    private static final int MAX_RETRY_COUNT = 5;
    private static final int LOCK_TIME_MINUTES = 10;

    /**
     * 检查用户是否被锁定
     */
    public void checkLock(String username) {
        String key = LOCK_KEY_PREFIX + username;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            int count = (Integer) value;
            if (count >= MAX_RETRY_COUNT) {
                throw new BusinessException("账号已锁定，请" + LOCK_TIME_MINUTES + "分钟后再试");
            }
        }
    }

    /**
     * 记录登录失败
     */
    public void recordFail(String username) {
        String key = LOCK_KEY_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 清除登录失败记录
     */
    public void clearLock(String username) {
        String key = LOCK_KEY_PREFIX + username;
        redisTemplate.delete(key);
    }
}

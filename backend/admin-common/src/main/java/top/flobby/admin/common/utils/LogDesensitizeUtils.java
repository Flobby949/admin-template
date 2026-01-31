package top.flobby.admin.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志脱敏工具类
 * 用于对敏感信息进行脱敏处理，防止敏感数据泄露到日志中
 */
public final class LogDesensitizeUtils {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("(?i)(\"?password\"?\\s*[:=]\\s*)(\".*?\"|[^,\\s}\\]]+)");
    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("(?i)(\"?(accessToken|refreshToken|token|authorization)\"?\\s*[:=]\\s*)(\".*?\"|[^,\\s}\\]]+)");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?<!\\d)(1\\d{2})\\d{4}(\\d{4})(?!\\d)");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("([A-Za-z0-9._%+-])([A-Za-z0-9._%+-]*)(@[^\\s,}\\]]+)");
    private static final Pattern ID_CARD_18_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{6})\\d{8}(\\d{4})(?!\\d)");
    private static final Pattern ID_CARD_15_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{6})\\d{6}(\\d{3})(?!\\d)");

    private LogDesensitizeUtils() {
    }

    /**
     * 对输入字符串进行全面脱敏处理
     *
     * @param input 原始字符串
     * @return 脱敏后的字符串
     */
    public static String desensitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String result = input;
        result = maskPassword(result);
        result = maskToken(result);
        result = maskPhone(result);
        result = maskEmail(result);
        result = maskIdCard(result);
        return result;
    }

    /**
     * 密码脱敏
     * 将 password=xxx 或 "password":"xxx" 替换为 password=***
     */
    public static String maskPassword(String input) {
        return replacePattern(input, PASSWORD_PATTERN, "$1\"***\"");
    }

    /**
     * Token 脱敏
     * 将 token/accessToken/refreshToken/authorization 的值替换为 ***
     */
    public static String maskToken(String input) {
        return replacePattern(input, TOKEN_PATTERN, "$1\"***\"");
    }

    /**
     * 手机号脱敏
     * 将手机号中间4位替换为 ****，如 138****1234
     */
    public static String maskPhone(String input) {
        return replacePattern(input, PHONE_PATTERN, "$1****$2");
    }

    /**
     * 邮箱脱敏
     * 保留首字符和@后面的域名，中间用 *** 替换
     */
    public static String maskEmail(String input) {
        return replacePattern(input, EMAIL_PATTERN, "$1***$3");
    }

    /**
     * 身份证号脱敏
     * 18位身份证：保留前6位和后4位，中间8位用 ******** 替换
     * 15位身份证：保留前6位和后3位，中间6位用 ****** 替换
     */
    public static String maskIdCard(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String result = ID_CARD_18_PATTERN.matcher(input).replaceAll("$1********$2");
        return ID_CARD_15_PATTERN.matcher(result).replaceAll("$1******$2");
    }

    private static String replacePattern(String input, Pattern pattern, String replacement) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll(replacement);
    }
}

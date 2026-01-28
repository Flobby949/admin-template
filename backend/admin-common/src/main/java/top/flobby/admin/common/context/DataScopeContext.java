package top.flobby.admin.common.context;

/**
 * 数据权限上下文
 * <p>
 * 使用 ThreadLocal 存储当前请求的数据权限信息
 *
 * @author flobby
 * @date 2026-01-28
 */
public final class DataScopeContext {

    private static final ThreadLocal<DataScopeInfo> CONTEXT = new ThreadLocal<>();

    private DataScopeContext() {
        // 工具类，禁止实例化
    }

    /**
     * 设置数据权限上下文
     *
     * @param info 数据权限信息
     */
    public static void set(DataScopeInfo info) {
        CONTEXT.set(info);
    }

    /**
     * 获取数据权限上下文
     *
     * @return 数据权限信息，如果未设置则返回 null
     */
    public static DataScopeInfo get() {
        return CONTEXT.get();
    }

    /**
     * 清除数据权限上下文
     * <p>
     * 必须在请求结束时调用，避免 ThreadLocal 内存泄漏
     */
    public static void clear() {
        CONTEXT.remove();
    }
}

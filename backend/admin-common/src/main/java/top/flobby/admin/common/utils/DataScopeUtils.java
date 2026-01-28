package top.flobby.admin.common.utils;

import jakarta.persistence.criteria.*;
import top.flobby.admin.common.context.DataScopeContext;
import top.flobby.admin.common.context.DataScopeInfo;

import java.util.Set;

/**
 * 数据权限工具类
 * <p>
 * 使用 JPA Criteria API 构建数据权限过滤条件
 *
 * @author flobby
 * @date 2026-01-28
 */
public final class DataScopeUtils {

    private DataScopeUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 构建用户数据权限过滤条件
     * <p>
     * 根据当前用户的数据权限范围，生成对应的 JPA Predicate
     *
     * @param root          User 实体的 Root
     * @param cb            CriteriaBuilder
     * @param query         CriteriaQuery
     * @param userDeptClass UserDept 实体类
     * @param <T>           查询结果类型
     * @return 数据权限过滤条件，如果无需过滤则返回 conjunction (恒真条件)
     */
    public static <T> Predicate buildUserDataScopePredicate(
            Root<?> root,
            CriteriaBuilder cb,
            CriteriaQuery<T> query,
            Class<?> userDeptClass) {

        DataScopeInfo info = DataScopeContext.get();

        // 未设置数据权限上下文或权限范围为"全部数据"，不进行过滤
        if (info == null || info.getDataScope() == null || info.getDataScope() == 1) {
            return cb.conjunction();
        }

        Integer dataScope = info.getDataScope();
        Long userId = info.getUserId();
        Set<Long> allowedDeptIds = info.getAllowedDeptIds();

        // 4 - 仅本人：user.id = currentUserId
        if (dataScope == 4) {
            if (userId == null) {
                // 如果用户ID为空，返回恒假条件（不返回任何数据）
                return cb.disjunction();
            }
            return cb.equal(root.get("id"), userId);
        }

        // 2 - 本部门及下级 或 3 - 仅本部门：通过 UserDept 关联过滤
        if (dataScope == 2 || dataScope == 3) {
            if (allowedDeptIds == null || allowedDeptIds.isEmpty()) {
                // 如果部门ID集合为空，返回恒假条件
                return cb.disjunction();
            }

            // 创建子查询：SELECT userId FROM UserDept WHERE deptId IN (allowedDeptIds)
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<?> userDeptRoot = subquery.from(userDeptClass);
            subquery.select(userDeptRoot.get("userId"))
                    .where(userDeptRoot.get("deptId").in(allowedDeptIds));

            // user.id IN (subquery)
            return root.get("id").in(subquery);
        }

        // 其他情况（理论上不应该出现），返回恒真条件
        return cb.conjunction();
    }

    /**
     * 构建通用实体的数据权限过滤条件
     * <p>
     * 适用于直接包含 deptId 字段的实体
     *
     * @param root      实体的 Root
     * @param cb        CriteriaBuilder
     * @param deptField 部门字段名
     * @param userField 用户字段名
     * @param <T>       查询结果类型
     * @return 数据权限过滤条件
     */
    public static <T> Predicate buildGenericDataScopePredicate(
            Root<?> root,
            CriteriaBuilder cb,
            String deptField,
            String userField) {

        DataScopeInfo info = DataScopeContext.get();

        // 未设置数据权限上下文或权限范围为"全部数据"，不进行过滤
        if (info == null || info.getDataScope() == null || info.getDataScope() == 1) {
            return cb.conjunction();
        }

        Integer dataScope = info.getDataScope();
        Long userId = info.getUserId();
        Set<Long> allowedDeptIds = info.getAllowedDeptIds();

        // 4 - 仅本人
        if (dataScope == 4) {
            if (userId == null) {
                return cb.disjunction();
            }
            return cb.equal(root.get(userField), userId);
        }

        // 2 - 本部门及下级 或 3 - 仅本部门
        if (dataScope == 2 || dataScope == 3) {
            if (allowedDeptIds == null || allowedDeptIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get(deptField).in(allowedDeptIds);
        }

        return cb.conjunction();
    }
}

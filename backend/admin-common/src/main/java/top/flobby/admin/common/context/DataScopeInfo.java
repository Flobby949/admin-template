package top.flobby.admin.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 数据权限上下文信息
 *
 * @author flobby
 * @date 2026-01-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeInfo {

    /**
     * 当前用户ID
     */
    private Long userId;

    /**
     * 数据权限范围
     * <ul>
     *   <li>1 - 全部数据</li>
     *   <li>2 - 本部门及下级</li>
     *   <li>3 - 仅本部门</li>
     *   <li>4 - 仅本人</li>
     * </ul>
     */
    private Integer dataScope;

    /**
     * 允许访问的部门ID集合
     * <p>
     * 对于 scope=2 和 scope=3，包含用户所属部门及其子部门（scope=2）或仅所属部门（scope=3）
     */
    private Set<Long> allowedDeptIds;
}

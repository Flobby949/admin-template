package top.flobby.admin.system.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 路由 VO
 * <p>
 * 用于前端动态路由加载，格式符合 Vue Router 规范
 * <p>
 * 字段说明：
 * - path: 路由路径，如 /system/user
 * - name: 路由名称，用于 router-link 和编程式导航
 * - component: 组件路径，如 Layout 或 system/user/index
 * - redirect: 重定向路径，目录类型时使用
 * - meta: 路由元信息，包含标题、图标、权限等
 * - children: 子路由列表
 *
 * @author flobby
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "路由响应")
public class RouterVO {

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "路由名称")
    private String name;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "重定向路径")
    private String redirect;

    @Schema(description = "路由元信息")
    private MetaVO meta;

    @Schema(description = "子路由")
    private List<RouterVO> children;

    /**
     * 路由元信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "路由元信息")
    public static class MetaVO {

        @Schema(description = "菜单标题")
        private String title;

        @Schema(description = "菜单图标")
        private String icon;

        @Schema(description = "是否隐藏")
        private Boolean hidden;

        @Schema(description = "是否缓存")
        private Boolean keepAlive;

        @Schema(description = "权限标识列表")
        private List<String> permissions;
    }
}

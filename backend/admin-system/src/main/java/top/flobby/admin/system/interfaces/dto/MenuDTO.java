package top.flobby.admin.system.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单数据传输对象
 * <p>
 * 职责：用于接收前端菜单管理请求，支持新增和编辑菜单操作
 * <p>
 * 使用场景：
 * - 新增菜单：前端提交菜单信息到后端
 * - 编辑菜单：前端提交修改后的菜单信息
 * - 参数验证：通过 Bean Validation 注解进行字段校验
 * <p>
 * 字段说明：
 * - parentId: 父菜单ID，0表示顶级菜单
 * - menuName: 菜单名称，必填，长度1-50字符
 * - menuType: 菜单类型，必填，1-目录，2-菜单，3-按钮
 * - routePath: 路由路径，菜单类型时必填
 * - component: 组件路径，菜单类型时使用
 * - permission: 权限标识，按钮类型时必填
 * - icon: 图标名称，目录和菜单类型使用
 * - sortOrder: 排序值，数字越小越靠前
 * - visible: 是否可见，0-隐藏，1-显示
 * - status: 状态，0-禁用，1-启用
 *
 * @author flobby
 * @date 2026-01-26
 */
@Data
@Schema(description = "菜单请求")
public class MenuDTO {

    @Schema(description = "父菜单ID，0表示顶级菜单")
    private Long parentId = 0L;

    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 1, max = 50, message = "菜单名称长度必须在1-50之间")
    @Schema(description = "菜单名称", required = true)
    private String menuName;

    @NotNull(message = "菜单类型不能为空")
    @Schema(description = "菜单类型：1-目录，2-菜单，3-按钮", required = true)
    private Integer menuType;

    @Schema(description = "路由路径（菜单类型时必填）")
    @Size(max = 200, message = "路由路径长度不能超过200")
    private String routePath;

    @Schema(description = "组件路径")
    @Size(max = 200, message = "组件路径长度不能超过200")
    private String component;

    @Schema(description = "权限标识（按钮类型时必填）")
    @Size(max = 100, message = "权限标识长度不能超过100")
    private String permission;

    @Schema(description = "图标名称")
    @Size(max = 100, message = "图标名称长度不能超过100")
    private String icon;

    @Schema(description = "排序值，数字越小越靠前")
    private Integer sortOrder = 0;

    @Schema(description = "是否可见：0-隐藏，1-显示")
    private Integer visible = 1;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status = 1;
}

package top.flobby.admin.generator.config;

import lombok.Data;

/**
 * 菜单配置
 */
@Data
public class MenuConfig {

    /**
     * 是否生成菜单SQL
     */
    private Boolean enabled = false;

    /**
     * 是否创建一级目录（新模块需要）
     */
    private Boolean createDirectory = false;

    /**
     * 一级目录ID
     */
    private Integer dirId;

    /**
     * 一级目录名称
     */
    private String dirName;

    /**
     * 一级目录图标
     */
    private String dirIcon = "Folder";

    /**
     * 一级目录排序
     */
    private Integer dirSort = 10;

    /**
     * 二级菜单起始ID
     */
    private Integer menuIdStart;

    /**
     * 菜单图标
     */
    private String menuIcon = "List";

    /**
     * 菜单排序起始值
     */
    private Integer menuSortStart = 1;

    /**
     * 按钮ID起始值
     */
    private Integer btnIdStart;

    /**
     * 获取下一个菜单ID
     */
    public Integer getNextMenuId(int index) {
        return menuIdStart != null ? menuIdStart + index * 10 : null;
    }

    /**
     * 获取下一个按钮起始ID
     */
    public Integer getNextBtnBaseId(int index) {
        Integer menuId = getNextMenuId(index);
        return menuId != null ? menuId + 1 : null;
    }

    /**
     * 获取下一个菜单排序
     */
    public Integer getNextMenuSort(int index) {
        return menuSortStart + index;
    }
}

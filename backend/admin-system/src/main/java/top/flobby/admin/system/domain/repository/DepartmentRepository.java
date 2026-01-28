package top.flobby.admin.system.domain.repository;

import top.flobby.admin.system.domain.entity.Department;

import java.util.List;
import java.util.Optional;

/**
 * 部门仓储接口
 * <p>
 * 定义部门领域对象的持久化操作
 */
public interface DepartmentRepository {

    /**
     * 根据ID查询部门
     *
     * @param id 部门ID
     * @return 部门实体
     */
    Optional<Department> findById(Long id);

    /**
     * 查询所有部门
     *
     * @return 部门列表
     */
    List<Department> findAll();

    /**
     * 根据父ID查询子部门
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<Department> findByParentId(Long parentId);

    /**
     * 根据祖级路径前缀查询所有子孙部门
     *
     * @param ancestorsPrefix 祖级路径前缀
     * @return 子孙部门列表
     */
    List<Department> findByAncestorsStartingWith(String ancestorsPrefix);

    /**
     * 检查是否存在子部门
     *
     * @param parentId 父部门ID
     * @return true-存在, false-不存在
     */
    boolean existsByParentId(Long parentId);

    /**
     * 检查部门名称是否存在(同父节点下)
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return true-存在, false-不存在
     */
    boolean existsByDeptNameAndParentIdAndDeleted(String deptName, Long parentId, Integer deleted);

    /**
     * 检查部门名称是否存在(同父节点下,排除指定ID)
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @param id       排除的部门ID
     * @return true-存在, false-不存在
     */
    boolean existsByDeptNameAndParentIdAndIdNotAndDeleted(String deptName, Long parentId, Long id, Integer deleted);

    /**
     * 保存部门
     *
     * @param department 部门实体
     * @return 保存后的部门实体
     */
    Department save(Department department);

    /**
     * 删除部门(逻辑删除)
     *
     * @param id 部门ID
     */
    void deleteById(Long id);

    /**
     * 批量更新祖级路径(前缀替换)
     *
     * @param oldPrefix 旧前缀
     * @param newPrefix 新前缀
     * @return 更新的记录数
     */
    int updateAncestorsByPrefix(String oldPrefix, String newPrefix);
}

package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.system.domain.entity.Department;

import java.util.List;

/**
 * Spring Data JPA 部门仓储
 */
public interface JpaDepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 查询未删除的所有部门
     */
    List<Department> findByDeleted(Integer deleted);

    /**
     * 根据父ID查询子部门
     */
    List<Department> findByParentIdAndDeleted(Long parentId, Integer deleted);

    /**
     * 根据祖级路径前缀查询子孙部门
     */
    @Query("SELECT d FROM Department d WHERE d.ancestors LIKE CONCAT(:prefix, '%') AND d.deleted = 0")
    List<Department> findByAncestorsStartingWith(@Param("prefix") String prefix);

    /**
     * 检查是否存在子部门
     */
    boolean existsByParentIdAndDeleted(Long parentId, Integer deleted);

    /**
     * 检查部门名称是否存在(同父节点下)
     */
    boolean existsByDeptNameAndParentIdAndDeleted(String deptName, Long parentId, Integer deleted);

    /**
     * 检查部门名称是否存在(同父节点下,排除指定ID)
     */
    boolean existsByDeptNameAndParentIdAndIdNotAndDeleted(String deptName, Long parentId, Long id, Integer deleted);

    /**
     * 逻辑删除
     */
    @Modifying
    @Query("UPDATE Department d SET d.deleted = 1 WHERE d.id = :id")
    void softDeleteById(@Param("id") Long id);

    /**
     * 批量更新祖级路径(前缀替换)
     */
    @Modifying
    @Query("UPDATE Department d SET d.ancestors = CONCAT(:newPrefix, SUBSTRING(d.ancestors, LENGTH(:oldPrefix) + 1)) " +
           "WHERE d.ancestors LIKE CONCAT(:oldPrefix, '%') AND d.deleted = 0")
    int updateAncestorsByPrefix(@Param("oldPrefix") String oldPrefix, @Param("newPrefix") String newPrefix);

    /**
     * 级联更新部门状态（包含所有子孙部门）
     */
    @Modifying
    @Query("UPDATE Department d SET d.status = :status WHERE d.deleted = 0 AND (d.id = :id OR d.ancestors LIKE CONCAT(:prefix, '%'))")
    int updateStatusCascade(@Param("id") Long id, @Param("prefix") String prefix, @Param("status") Integer status);

    /**
     * 统计指定部门列表中禁用部门的数量
     */
    long countByIdInAndStatusAndDeleted(List<Long> ids, Integer status, Integer deleted);
}

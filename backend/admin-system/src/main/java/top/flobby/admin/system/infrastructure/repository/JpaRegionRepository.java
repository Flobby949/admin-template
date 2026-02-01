package top.flobby.admin.system.infrastructure.repository;

import top.flobby.admin.system.domain.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 区域表 JPA 仓储接口
 *
 * @author Code Generator
 * @date 2026-02-01
 */
public interface JpaRegionRepository extends JpaRepository<Region, Long>,
        JpaSpecificationExecutor<Region> {

    /**
     * 根据区域名称查询
     */
    List<Region> findByNameContaining(String name);

    /**
     * 根据区域编码查询
     */
    List<Region> findByRegionCodeContaining(String regionCode);

    /**
     * 根据父级编码查询
     */
    List<Region> findByParentCodeContaining(String parentCode);

    /**
     * 根据状态：0-禁用，1-启用查询
     */
    List<Region> findByStatus(Integer status);

    /**
     * 根据删除标记查询
     */
    List<Region> findByDeleted(Integer deleted);
}

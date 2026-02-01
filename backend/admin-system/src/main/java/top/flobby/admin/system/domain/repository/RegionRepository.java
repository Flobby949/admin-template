package top.flobby.admin.system.domain.repository;

import top.flobby.admin.system.domain.entity.Region;

import java.util.List;
import java.util.Optional;

/**
 * 区域表仓储接口
 *
 * @author Code Generator
 * @date 2026-02-01
 */
public interface RegionRepository {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 实体
     */
    Optional<Region> findById(Long id);

    /**
     * 查询所有
     *
     * @return 实体列表
     */
    List<Region> findAll();

    /**
     * 保存实体
     *
     * @param entity 实体
     * @return 保存后的实体
     */
    Region save(Region entity);

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     */
    void deleteById(Long id);

    /**
     * 根据ID判断是否存在
     *
     * @param id 主键ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    /**
     * 根据区域名称查询
     *
     * @param name 区域名称
     * @return 实体列表
     */
    List<Region> findByName(String name);

    /**
     * 根据区域编码查询
     *
     * @param regionCode 区域编码
     * @return 实体列表
     */
    List<Region> findByRegionCode(String regionCode);

    /**
     * 根据父级编码查询
     *
     * @param parentCode 父级编码
     * @return 实体列表
     */
    List<Region> findByParentCode(String parentCode);

    /**
     * 根据状态：0-禁用，1-启用查询
     *
     * @param status 状态：0-禁用，1-启用
     * @return 实体列表
     */
    List<Region> findByStatus(Integer status);
}

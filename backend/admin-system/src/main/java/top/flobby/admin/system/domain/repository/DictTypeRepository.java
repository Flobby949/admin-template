package top.flobby.admin.system.domain.repository;

import top.flobby.admin.system.domain.entity.DictType;

import java.util.List;
import java.util.Optional;

/**
 * 字典类型仓储接口
 */
public interface DictTypeRepository {

    /**
     * 根据ID查询字典类型
     */
    Optional<DictType> findById(Long id);

    /**
     * 根据字典类型查询
     */
    Optional<DictType> findByType(String dictType);

    /**
     * 查询所有字典类型（未删除）
     */
    List<DictType> findAll();

    /**
     * 查询所有启用的字典类型
     */
    List<DictType> findAllEnabled();

    /**
     * 保存字典类型
     */
    DictType save(DictType dictType);

    /**
     * 删除字典类型（逻辑删除）
     */
    void deleteById(Long id);

    /**
     * 检查字典类型是否存在
     */
    boolean existsByType(String dictType);

    /**
     * 检查字典类型是否存在（排除指定ID）
     */
    boolean existsByTypeAndIdNot(String dictType, Long id);
}

package top.flobby.admin.system.domain.repository;

import top.flobby.admin.system.domain.entity.DictData;

import java.util.List;
import java.util.Optional;

/**
 * 字典数据仓储接口
 */
public interface DictDataRepository {

    /**
     * 根据ID查询字典数据
     */
    Optional<DictData> findById(Long id);

    /**
     * 根据字典类型查询所有数据（未删除）
     */
    List<DictData> findByDictType(String dictType);

    /**
     * 根据字典类型查询启用的数据
     */
    List<DictData> findEnabledByDictType(String dictType);

    /**
     * 保存字典数据
     */
    DictData save(DictData dictData);

    /**
     * 删除字典数据（逻辑删除）
     */
    void deleteById(Long id);

    /**
     * 检查字典数据是否存在（字典类型+值）
     */
    boolean existsByDictTypeAndValue(String dictType, String value);

    /**
     * 检查字典数据是否存在（字典类型+值，排除指定ID）
     */
    boolean existsByDictTypeAndValueAndIdNot(String dictType, String value, Long id);

    /**
     * 统计字典类型下的数据数量
     */
    long countByDictType(String dictType);
}

package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.system.domain.entity.DictData;

import java.util.List;

/**
 * Spring Data JPA 字典数据仓储
 */
public interface JpaDictDataRepository extends JpaRepository<DictData, Long> {

    List<DictData> findByDictTypeAndDeleted(String dictType, Integer deleted);

    @Query("SELECT dd FROM DictData dd WHERE dd.dictType = :dictType " +
           "AND dd.status = 1 AND dd.deleted = 0 ORDER BY dd.dictSort ASC, dd.id ASC")
    List<DictData> findEnabledByDictType(@Param("dictType") String dictType);

    boolean existsByDictTypeAndDictValueAndDeleted(String dictType, String dictValue, Integer deleted);

    boolean existsByDictTypeAndDictValueAndIdNotAndDeleted(String dictType, String dictValue, Long id, Integer deleted);

    long countByDictTypeAndDeleted(String dictType, Integer deleted);

    @Modifying
    @Query("UPDATE DictData dd SET dd.deleted = 1 WHERE dd.id = :id")
    void softDeleteById(@Param("id") Long id);
}

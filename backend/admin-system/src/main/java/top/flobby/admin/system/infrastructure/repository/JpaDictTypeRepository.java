package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.system.domain.entity.DictType;

import java.util.List;

/**
 * Spring Data JPA 字典类型仓储
 */
public interface JpaDictTypeRepository extends JpaRepository<DictType, Long> {

    DictType findByDictTypeAndDeleted(String dictType, Integer deleted);

    List<DictType> findByDeleted(Integer deleted);

    List<DictType> findByStatusAndDeleted(Integer status, Integer deleted);

    boolean existsByDictTypeAndDeleted(String dictType, Integer deleted);

    boolean existsByDictTypeAndIdNotAndDeleted(String dictType, Long id, Integer deleted);

    @Modifying
    @Query("UPDATE DictType dt SET dt.deleted = 1 WHERE dt.id = :id")
    void softDeleteById(@Param("id") Long id);
}

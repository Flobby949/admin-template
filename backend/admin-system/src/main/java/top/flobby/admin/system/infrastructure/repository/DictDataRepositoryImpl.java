package top.flobby.admin.system.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.flobby.admin.system.domain.entity.DictData;
import top.flobby.admin.system.domain.repository.DictDataRepository;

import java.util.List;
import java.util.Optional;

/**
 * 字典数据仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DictDataRepositoryImpl implements DictDataRepository {

    private final JpaDictDataRepository jpaDictDataRepository;

    @Override
    public Optional<DictData> findById(Long id) {
        return jpaDictDataRepository.findById(id)
                .filter(dictData -> dictData.getDeleted() == 0);
    }

    @Override
    public List<DictData> findByDictType(String dictType) {
        return jpaDictDataRepository.findByDictTypeAndDeleted(dictType, 0);
    }

    @Override
    public List<DictData> findEnabledByDictType(String dictType) {
        return jpaDictDataRepository.findEnabledByDictType(dictType);
    }

    @Override
    public DictData save(DictData dictData) {
        return jpaDictDataRepository.save(dictData);
    }

    @Override
    public void deleteById(Long id) {
        jpaDictDataRepository.softDeleteById(id);
    }

    @Override
    public boolean existsByDictTypeAndValue(String dictType, String value) {
        return jpaDictDataRepository.existsByDictTypeAndDictValueAndDeleted(dictType, value, 0);
    }

    @Override
    public boolean existsByDictTypeAndValueAndIdNot(String dictType, String value, Long id) {
        return jpaDictDataRepository.existsByDictTypeAndDictValueAndIdNotAndDeleted(dictType, value, id, 0);
    }

    @Override
    public long countByDictType(String dictType) {
        return jpaDictDataRepository.countByDictTypeAndDeleted(dictType, 0);
    }
}

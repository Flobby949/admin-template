package top.flobby.admin.system.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.flobby.admin.system.domain.entity.DictType;
import top.flobby.admin.system.domain.repository.DictTypeRepository;

import java.util.List;
import java.util.Optional;

/**
 * 字典类型仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DictTypeRepositoryImpl implements DictTypeRepository {

    private final JpaDictTypeRepository jpaDictTypeRepository;

    @Override
    public Optional<DictType> findById(Long id) {
        return jpaDictTypeRepository.findById(id)
                .filter(dictType -> dictType.getDeleted() == 0);
    }

    @Override
    public Optional<DictType> findByType(String dictType) {
        return Optional.ofNullable(jpaDictTypeRepository.findByDictTypeAndDeleted(dictType, 0));
    }

    @Override
    public List<DictType> findAll() {
        return jpaDictTypeRepository.findByDeleted(0);
    }

    @Override
    public List<DictType> findAllEnabled() {
        return jpaDictTypeRepository.findByStatusAndDeleted(1, 0);
    }

    @Override
    public DictType save(DictType dictType) {
        return jpaDictTypeRepository.save(dictType);
    }

    @Override
    public void deleteById(Long id) {
        jpaDictTypeRepository.softDeleteById(id);
    }

    @Override
    public boolean existsByType(String dictType) {
        return jpaDictTypeRepository.existsByDictTypeAndDeleted(dictType, 0);
    }

    @Override
    public boolean existsByTypeAndIdNot(String dictType, Long id) {
        return jpaDictTypeRepository.existsByDictTypeAndIdNotAndDeleted(dictType, id, 0);
    }
}

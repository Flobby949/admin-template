package top.flobby.admin.system.infrastructure.repository;

import top.flobby.admin.system.domain.entity.Region;
import top.flobby.admin.system.domain.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 区域表仓储实现
 *
 * @author Code Generator
 * @date 2026-02-01
 */
@Repository
@RequiredArgsConstructor
public class RegionRepositoryImpl implements RegionRepository {

    private final JpaRegionRepository jpaRepository;

    @Override
    public Optional<Region> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Region> findAll() {
        return jpaRepository.findByDeleted(0);
    }

    @Override
    public Region save(Region entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Region> findByName(String name) {
        return jpaRepository.findByNameContaining(name);
    }

    @Override
    public List<Region> findByRegionCode(String regionCode) {
        return jpaRepository.findByRegionCodeContaining(regionCode);
    }

    @Override
    public List<Region> findByParentCode(String parentCode) {
        return jpaRepository.findByParentCodeContaining(parentCode);
    }

    @Override
    public List<Region> findByStatus(Integer status) {
        return jpaRepository.findByStatus(status);
    }
}

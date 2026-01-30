package top.flobby.admin.system.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.flobby.admin.system.domain.entity.Department;
import top.flobby.admin.system.domain.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;

/**
 * 部门仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final JpaDepartmentRepository jpaDepartmentRepository;

    @Override
    public Optional<Department> findById(Long id) {
        return jpaDepartmentRepository.findById(id)
                .filter(dept -> dept.getDeleted() == 0);
    }

    @Override
    public List<Department> findAll() {
        return jpaDepartmentRepository.findByDeleted(0);
    }

    @Override
    public List<Department> findByParentId(Long parentId) {
        return jpaDepartmentRepository.findByParentIdAndDeleted(parentId, 0);
    }

    @Override
    public List<Department> findByAncestorsStartingWith(String ancestorsPrefix) {
        return jpaDepartmentRepository.findByAncestorsStartingWith(ancestorsPrefix);
    }

    @Override
    public boolean existsByParentId(Long parentId) {
        return jpaDepartmentRepository.existsByParentIdAndDeleted(parentId, 0);
    }

    @Override
    public boolean existsByDeptNameAndParentIdAndDeleted(String deptName, Long parentId, Integer deleted) {
        return jpaDepartmentRepository.existsByDeptNameAndParentIdAndDeleted(deptName, parentId, deleted);
    }

    @Override
    public boolean existsByDeptNameAndParentIdAndIdNotAndDeleted(String deptName, Long parentId, Long id, Integer deleted) {
        return jpaDepartmentRepository.existsByDeptNameAndParentIdAndIdNotAndDeleted(deptName, parentId, id, deleted);
    }

    @Override
    public Department save(Department department) {
        return jpaDepartmentRepository.save(department);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaDepartmentRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    public int updateAncestorsByPrefix(String oldPrefix, String newPrefix) {
        return jpaDepartmentRepository.updateAncestorsByPrefix(oldPrefix, newPrefix);
    }

    @Override
    @Transactional
    public int updateStatusCascade(Long id, String prefix, Integer status) {
        return jpaDepartmentRepository.updateStatusCascade(id, prefix, status);
    }

    @Override
    public long countByIdInAndStatusAndDeleted(List<Long> ids, Integer status, Integer deleted) {
        return jpaDepartmentRepository.countByIdInAndStatusAndDeleted(ids, status, deleted);
    }
}

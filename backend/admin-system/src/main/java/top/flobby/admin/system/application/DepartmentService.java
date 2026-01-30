package top.flobby.admin.system.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.system.domain.entity.Department;
import top.flobby.admin.system.domain.repository.DepartmentRepository;
import top.flobby.admin.system.infrastructure.repository.JpaUserDeptRepository;
import top.flobby.admin.system.interfaces.dto.DepartmentDTO;
import top.flobby.admin.system.interfaces.vo.DepartmentVO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务
 * <p>
 * 职责:
 * - 部门CRUD操作
 * - 树形结构维护
 * - 业务规则校验
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final JpaUserDeptRepository jpaUserDeptRepository;

    /**
     * 最大层级深度
     */
    private static final int MAX_LEVEL = 10;

    /**
     * 获取部门树
     *
     * @return 部门树列表
     */
    public List<DepartmentVO> listDepartmentTree() {
        List<Department> departments = departmentRepository.findAll();
        return buildDepartmentTree(departments, 0L);
    }

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    public DepartmentVO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));
        return toDepartmentVO(department);
    }

    /**
     * 创建部门
     *
     * @param dto 部门DTO
     * @return 部门ID
     */
    @Transactional
    public Long createDepartment(DepartmentDTO dto) {
        // 校验父部门
        Department parent = null;
        if (dto.getParentId() != null && dto.getParentId() != 0) {
            parent = departmentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new BusinessException("父部门不存在"));
        }

        // 校验部门名称唯一性(同父节点下)
        if (departmentRepository.existsByDeptNameAndParentIdAndDeleted(
                dto.getDeptName(), dto.getParentId(), 0)) {
            throw new BusinessException("同级部门名称已存在");
        }

        // 计算ancestors
        String ancestors = calculateAncestors(parent);

        // 校验层级深度
        int level = ancestors.isEmpty() ? 1 : ancestors.split(",").length + 1;
        if (level > MAX_LEVEL) {
            throw new BusinessException("部门层级不能超过" + MAX_LEVEL + "级");
        }

        // 创建部门
        Department department = new Department();
        department.setParentId(dto.getParentId());
        department.setAncestors(ancestors);
        department.setDeptName(dto.getDeptName());
        department.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        department.setLeader(dto.getLeader());
        department.setPhone(dto.getPhone());
        department.setEmail(dto.getEmail());
        department.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        department.setDeleted(0);

        Department savedDepartment = departmentRepository.save(department);
        log.info("创建部门成功: id={}, name={}", savedDepartment.getId(), savedDepartment.getDeptName());

        return savedDepartment.getId();
    }

    /**
     * 更新部门
     *
     * @param dto 部门DTO
     */
    @Transactional
    public void updateDepartment(DepartmentDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("部门ID不能为空");
        }

        Department department = departmentRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException("部门不存在"));

        // 校验部门名称唯一性(同父节点下,排除自己)
        if (departmentRepository.existsByDeptNameAndParentIdAndIdNotAndDeleted(
                dto.getDeptName(), dto.getParentId(), dto.getId(), 0)) {
            throw new BusinessException("同级部门名称已存在");
        }

        // 检查是否修改了父部门
        boolean parentChanged = !department.getParentId().equals(dto.getParentId());

        if (parentChanged) {
            // 校验不能将部门移动到自己或子孙部门下
            if (dto.getParentId().equals(dto.getId())) {
                throw new BusinessException("不能将部门移动到自己下面");
            }

            // 检查是否移动到子孙部门
            if (isDescendant(dto.getId(), dto.getParentId())) {
                throw new BusinessException("不能将部门移动到其子孙部门下");
            }

            // 获取新父部门
            Department newParent = null;
            if (dto.getParentId() != 0) {
                newParent = departmentRepository.findById(dto.getParentId())
                        .orElseThrow(() -> new BusinessException("父部门不存在"));
            }

            // 计算新的ancestors
            String newAncestors = calculateAncestors(newParent);

            // 校验层级深度
            int newLevel = newAncestors.isEmpty() ? 1 : newAncestors.split(",").length + 1;
            if (newLevel > MAX_LEVEL) {
                throw new BusinessException("部门层级不能超过" + MAX_LEVEL + "级");
            }

            // 更新当前部门的ancestors
            String oldAncestors = department.getAncestors();
            department.setAncestors(newAncestors);

            // 批量更新子孙部门的ancestors
            String oldPrefix = oldAncestors.isEmpty() ? String.valueOf(dto.getId()) : oldAncestors + "," + dto.getId();
            String newPrefix = newAncestors.isEmpty() ? String.valueOf(dto.getId()) : newAncestors + "," + dto.getId();
            departmentRepository.updateAncestorsByPrefix(oldPrefix, newPrefix);
        }

        // 更新部门信息
        department.setParentId(dto.getParentId());
        department.setDeptName(dto.getDeptName());
        department.setSortOrder(dto.getSortOrder());
        department.setLeader(dto.getLeader());
        department.setPhone(dto.getPhone());
        department.setEmail(dto.getEmail());
        department.setStatus(dto.getStatus());

        departmentRepository.save(department);
        log.info("更新部门成功: id={}, name={}", department.getId(), department.getDeptName());
    }

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));

        // 检查是否有子部门
        if (departmentRepository.existsByParentId(id)) {
            throw new BusinessException("该部门下存在子部门,无法删除");
        }

        // 检查是否有用户
        if (jpaUserDeptRepository.existsByDeptId(id)) {
            throw new BusinessException("该部门下存在用户,无法删除");
        }

        // 逻辑删除
        departmentRepository.deleteById(id);
        log.info("删除部门成功: id={}, name={}", id, department.getDeptName());
    }

    /**
     * 更新部门状态
     *
     * @param id     部门ID
     * @param status 状态
     */
    @Transactional
    public void updateDepartmentStatus(Long id, Integer status) {
        // 校验状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（禁用）或1（启用）");
        }

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));

        if (status == 0) {
            // 禁用操作：级联禁用所有子孙部门
            String ancestorsPrefix = calculateDescendantsPrefix(department);
            int updatedCount = departmentRepository.updateStatusCascade(id, ancestorsPrefix, 0);
            log.info("级联禁用部门成功: id={}, 影响部门数={}", id, updatedCount);
        } else {
            // 启用操作：仅启用当前部门（不级联）
            department.setStatus(status);
            departmentRepository.save(department);
            log.info("启用部门成功: id={}", id);
        }
    }

    /**
     * 计算子孙部门查询前缀
     *
     * @param department 部门
     * @return 子孙部门前缀
     */
    private String calculateDescendantsPrefix(Department department) {
        String ancestors = department.getAncestors();
        if (ancestors == null || ancestors.isEmpty()) {
            return String.valueOf(department.getId());
        }
        return ancestors + "," + department.getId();
    }

    /**
     * 计算ancestors路径
     *
     * @param parent 父部门
     * @return ancestors路径
     */
    private String calculateAncestors(Department parent) {
        if (parent == null) {
            return "";
        }

        String parentAncestors = parent.getAncestors();
        if (parentAncestors == null || parentAncestors.isEmpty()) {
            return String.valueOf(parent.getId());
        }

        return parentAncestors + "," + parent.getId();
    }

    /**
     * 检查是否为子孙部门
     *
     * @param ancestorId   祖先部门ID
     * @param descendantId 子孙部门ID
     * @return true-是子孙部门, false-不是
     */
    private boolean isDescendant(Long ancestorId, Long descendantId) {
        Department descendant = departmentRepository.findById(descendantId).orElse(null);
        if (descendant == null) {
            return false;
        }

        String ancestors = descendant.getAncestors();
        if (ancestors == null || ancestors.isEmpty()) {
            return false;
        }

        return ancestors.contains(String.valueOf(ancestorId));
    }

    /**
     * 构建部门树
     *
     * @param departments 部门列表
     * @param parentId    父部门ID
     * @return 部门树
     */
    private List<DepartmentVO> buildDepartmentTree(List<Department> departments, Long parentId) {
        List<DepartmentVO> tree = new ArrayList<>();

        for (Department department : departments) {
            if (department.getParentId().equals(parentId)) {
                DepartmentVO vo = toDepartmentVO(department);
                vo.setChildren(buildDepartmentTree(departments, department.getId()));
                tree.add(vo);
            }
        }

        // 按排序字段排序
        tree.sort((a, b) -> {
            int orderA = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int orderB = b.getSortOrder() != null ? b.getSortOrder() : 0;
            return Integer.compare(orderA, orderB);
        });

        return tree;
    }

    /**
     * 转换为VO
     *
     * @param department 部门实体
     * @return 部门VO
     */
    private DepartmentVO toDepartmentVO(Department department) {
        DepartmentVO vo = new DepartmentVO();
        vo.setId(department.getId());
        vo.setParentId(department.getParentId());
        vo.setAncestors(department.getAncestors());
        vo.setDeptName(department.getDeptName());
        vo.setSortOrder(department.getSortOrder());
        vo.setLeader(department.getLeader());
        vo.setPhone(department.getPhone());
        vo.setEmail(department.getEmail());
        vo.setStatus(department.getStatus());
        vo.setCreateTime(department.getCreateTime());
        vo.setUpdateTime(department.getUpdateTime());
        vo.setCreateBy(department.getCreateBy());
        vo.setUpdateBy(department.getUpdateBy());
        return vo;
    }
}

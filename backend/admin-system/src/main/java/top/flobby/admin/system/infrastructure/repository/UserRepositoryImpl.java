package top.flobby.admin.system.infrastructure.repository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.repository.UserRepository;
import top.flobby.admin.system.interfaces.query.UserQuery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(jpaUserRepository.findByUsername(username));
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(jpaUserRepository.findByEmail(email));
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return Optional.ofNullable(jpaUserRepository.findByPhone(phone));
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        jpaUserRepository.logicalDeleteById(id);
    }

    @Override
    public Page<User> findByQuery(UserQuery query, Pageable pageable) {
        Specification<User> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 未删除的用户
            predicates.add(criteriaBuilder.equal(root.get("deleted"), 0));

            // 用户名模糊查询
            if (StringUtils.hasText(query.getUsername())) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + query.getUsername() + "%"));
            }

            // 手机号精确查询
            if (StringUtils.hasText(query.getPhone())) {
                predicates.add(criteriaBuilder.equal(root.get("phone"), query.getPhone()));
            }

            // 邮箱精确查询
            if (StringUtils.hasText(query.getEmail())) {
                predicates.add(criteriaBuilder.equal(root.get("email"), query.getEmail()));
            }

            // 状态查询
            if (query.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.getStatus()));
            }

            // 创建时间范围查询
            if (StringUtils.hasText(query.getStartTime())) {
                LocalDateTime startTime = LocalDateTime.parse(query.getStartTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), startTime));
            }

            if (StringUtils.hasText(query.getEndTime())) {
                LocalDateTime endTime = LocalDateTime.parse(query.getEndTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), endTime));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return jpaUserRepository.findAll(spec, pageable);
    }

    @Override
    public List<User> findByRoleId(Long roleId) {
        return jpaUserRepository.findByRoleId(roleId);
    }

    // TODO: Phase 6 实现 - 需要 UserDept 实体
    // @Override
    // public List<User> findByDeptId(Long deptId) {
    //     return jpaUserRepository.findByDeptId(deptId);
    // }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsernameAndDeletedEquals(username, 0);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmailAndDeletedEquals(email, 0);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return jpaUserRepository.existsByPhoneAndDeletedEquals(phone, 0);
    }
}

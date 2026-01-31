package top.flobby.admin.system;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import top.flobby.admin.common.context.DataScopeContext;
import top.flobby.admin.common.context.DataScopeInfo;
import top.flobby.admin.common.utils.DataScopeUtils;
import top.flobby.admin.system.domain.entity.Department;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.entity.UserDept;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多部门数据权限并集测试
 *
 * @author flobby
 * @date 2026-01-28
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("多部门数据权限并集测试")
class MultiDeptScopeTest {

    @Autowired
    private EntityManager entityManager;

    private CriteriaBuilder cb;
    private Long testUserId1;
    private Long testUserId2;
    private Long testUserId3;
    private Long deptId1;
    private Long deptId2;
    private Long deptId3;

    @BeforeEach
    void setUp() {
        cb = entityManager.getCriteriaBuilder();

        // 创建测试部门
        Department dept1 = new Department();
        dept1.setDeptName("技术部");
        dept1.setParentId(0L);
        dept1.setAncestors("0");
        dept1.setStatus(1);
        dept1.setDeleted(0);
        entityManager.persist(dept1);

        Department dept2 = new Department();
        dept2.setDeptName("产品部");
        dept2.setParentId(0L);
        dept2.setAncestors("0");
        dept2.setStatus(1);
        dept2.setDeleted(0);
        entityManager.persist(dept2);

        Department dept3 = new Department();
        dept3.setDeptName("市场部");
        dept3.setParentId(0L);
        dept3.setAncestors("0");
        dept3.setStatus(1);
        dept3.setDeleted(0);
        entityManager.persist(dept3);

        entityManager.flush();
        deptId1 = dept1.getId();
        deptId2 = dept2.getId();
        deptId3 = dept3.getId();

        // 创建测试用户
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password");
        user1.setRealName("用户1");
        user1.setStatus(1);
        user1.setDeleted(0);
        entityManager.persist(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password");
        user2.setRealName("用户2");
        user2.setStatus(1);
        user2.setDeleted(0);
        entityManager.persist(user2);

        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password");
        user3.setRealName("用户3");
        user3.setStatus(1);
        user3.setDeleted(0);
        entityManager.persist(user3);

        entityManager.flush();
        testUserId1 = user1.getId();
        testUserId2 = user2.getId();
        testUserId3 = user3.getId();

        // 创建用户部门关联
        // user1 属于技术部和产品部（多部门）
        UserDept userDept1_1 = new UserDept();
        userDept1_1.setUserId(testUserId1);
        userDept1_1.setDeptId(deptId1);
        entityManager.persist(userDept1_1);

        UserDept userDept1_2 = new UserDept();
        userDept1_2.setUserId(testUserId1);
        userDept1_2.setDeptId(deptId2);
        entityManager.persist(userDept1_2);

        // user2 属于产品部
        UserDept userDept2 = new UserDept();
        userDept2.setUserId(testUserId2);
        userDept2.setDeptId(deptId2);
        entityManager.persist(userDept2);

        // user3 属于市场部
        UserDept userDept3 = new UserDept();
        userDept3.setUserId(testUserId3);
        userDept3.setDeptId(deptId3);
        entityManager.persist(userDept3);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("多部门用户 - dataScope=3 (仅本部门): 返回两个部门的并集")
    void testMultiDeptScopeDept() {
        // 设置数据权限上下文：仅本部门（技术部 + 产品部）
        Set<Long> allowedDeptIds = new HashSet<>();
        allowedDeptIds.add(deptId1); // 技术部
        allowedDeptIds.add(deptId2); // 产品部
        DataScopeInfo info = new DataScopeInfo(testUserId1, 3, allowedDeptIds);
        DataScopeContext.set(info);

        try {
            // 构建查询
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            // 应用数据权限过滤
            Predicate dataScopePredicate = DataScopeUtils.buildUserDataScopePredicate(
                    root, cb, query, UserDept.class);

            query.select(root).where(
                    cb.and(
                            cb.equal(root.get("deleted"), 0),
                            dataScopePredicate
                    )
            );

            // 执行查询
            List<User> users = entityManager.createQuery(query).getResultList();

            // 验证：应该返回技术部和产品部的用户（user1 和 user2）
            assertEquals(2, users.size());
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(testUserId1)));
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(testUserId2)));
        } finally {
            DataScopeContext.clear();
        }
    }

    @Test
    @DisplayName("多部门用户 - dataScope=2 (本部门及下级): 返回两个部门树的并集")
    void testMultiDeptScopeDeptAndChild() {
        // 创建子部门
        Department subDept1 = new Department();
        subDept1.setDeptName("技术部-研发组");
        subDept1.setParentId(deptId1);
        subDept1.setAncestors("0," + deptId1);
        subDept1.setStatus(1);
        subDept1.setDeleted(0);
        entityManager.persist(subDept1);

        Department subDept2 = new Department();
        subDept2.setDeptName("产品部-设计组");
        subDept2.setParentId(deptId2);
        subDept2.setAncestors("0," + deptId2);
        subDept2.setStatus(1);
        subDept2.setDeleted(0);
        entityManager.persist(subDept2);

        entityManager.flush();
        Long subDeptId1 = subDept1.getId();
        Long subDeptId2 = subDept2.getId();

        // 创建子部门用户
        User user4 = new User();
        user4.setUsername("user4");
        user4.setPassword("password");
        user4.setRealName("用户4");
        user4.setStatus(1);
        user4.setDeleted(0);
        entityManager.persist(user4);

        User user5 = new User();
        user5.setUsername("user5");
        user5.setPassword("password");
        user5.setRealName("用户5");
        user5.setStatus(1);
        user5.setDeleted(0);
        entityManager.persist(user5);

        entityManager.flush();
        Long testUserId4 = user4.getId();
        Long testUserId5 = user5.getId();

        // 关联用户到子部门
        UserDept userDept4 = new UserDept();
        userDept4.setUserId(testUserId4);
        userDept4.setDeptId(subDeptId1);
        entityManager.persist(userDept4);

        UserDept userDept5 = new UserDept();
        userDept5.setUserId(testUserId5);
        userDept5.setDeptId(subDeptId2);
        entityManager.persist(userDept5);

        entityManager.flush();
        entityManager.clear();

        // 设置数据权限上下文：本部门及下级（技术部 + 产品部 + 子部门）
        Set<Long> allowedDeptIds = new HashSet<>();
        allowedDeptIds.add(deptId1); // 技术部
        allowedDeptIds.add(deptId2); // 产品部
        allowedDeptIds.add(subDeptId1); // 技术部-研发组
        allowedDeptIds.add(subDeptId2); // 产品部-设计组
        DataScopeInfo info = new DataScopeInfo(testUserId1, 2, allowedDeptIds);
        DataScopeContext.set(info);

        try {
            // 构建查询
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            // 应用数据权限过滤
            Predicate dataScopePredicate = DataScopeUtils.buildUserDataScopePredicate(
                    root, cb, query, UserDept.class);

            query.select(root).where(
                    cb.and(
                            cb.equal(root.get("deleted"), 0),
                            dataScopePredicate
                    )
            );

            // 执行查询
            List<User> users = entityManager.createQuery(query).getResultList();

            // 验证：应该返回技术部、产品部及其子部门的所有用户（user1, user2, user4, user5）
            assertEquals(4, users.size());
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(testUserId1)));
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(testUserId2)));
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(testUserId4)));
            assertTrue(users.stream().anyMatch(u -> u.getId().equals(testUserId5)));
        } finally {
            DataScopeContext.clear();
        }
    }

    @Test
    @DisplayName("单部门用户 - 验证不会返回其他部门的数据")
    void testSingleDeptScopeIsolation() {
        // 设置数据权限上下文：仅本部门（仅市场部）
        Set<Long> allowedDeptIds = new HashSet<>();
        allowedDeptIds.add(deptId3); // 市场部
        DataScopeInfo info = new DataScopeInfo(testUserId3, 3, allowedDeptIds);
        DataScopeContext.set(info);

        try {
            // 构建查询
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            // 应用数据权限过滤
            Predicate dataScopePredicate = DataScopeUtils.buildUserDataScopePredicate(
                    root, cb, query, UserDept.class);

            query.select(root).where(
                    cb.and(
                            cb.equal(root.get("deleted"), 0),
                            dataScopePredicate
                    )
            );

            // 执行查询
            List<User> users = entityManager.createQuery(query).getResultList();

            // 验证：应该只返回市场部的用户（user3）
            assertEquals(1, users.size());
            assertEquals(testUserId3, users.get(0).getId());
        } finally {
            DataScopeContext.clear();
        }
    }
}

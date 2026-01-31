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
 * 数据权限 SQL 条件构建测试
 *
 * @author flobby
 * @date 2026-01-28
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("数据权限 SQL 条件构建测试")
class DataScopeTest {

    @Autowired
    private EntityManager entityManager;

    private CriteriaBuilder cb;
    private Long testUserId1;
    private Long testUserId2;
    private Long testUserId3;
    private Long deptId1;
    private Long deptId2;

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

        entityManager.flush();
        deptId1 = dept1.getId();
        deptId2 = dept2.getId();

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
        UserDept userDept1 = new UserDept();
        userDept1.setUserId(testUserId1);
        userDept1.setDeptId(deptId1);
        entityManager.persist(userDept1);

        UserDept userDept2 = new UserDept();
        userDept2.setUserId(testUserId2);
        userDept2.setDeptId(deptId2);
        entityManager.persist(userDept2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("dataScope=1 (全部数据): 不进行过滤")
    void testDataScopeAll() {
        // 设置数据权限上下文：全部数据
        DataScopeInfo info = new DataScopeInfo(testUserId1, 1, new HashSet<>());
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

            // 验证：应该返回所有用户
            assertEquals(3, users.size());
        } finally {
            DataScopeContext.clear();
        }
    }

    @Test
    @DisplayName("dataScope=4 (仅本人): user.id = currentUserId")
    void testDataScopeSelf() {
        // 设置数据权限上下文：仅本人
        DataScopeInfo info = new DataScopeInfo(testUserId1, 4, new HashSet<>());
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

            // 验证：应该只返回当前用户
            assertEquals(1, users.size());
            assertEquals(testUserId1, users.get(0).getId());
        } finally {
            DataScopeContext.clear();
        }
    }

    @Test
    @DisplayName("dataScope=3 (仅本部门): 用户在允许的部门列表中")
    void testDataScopeDept() {
        // 设置数据权限上下文：仅本部门（技术部）
        Set<Long> allowedDeptIds = new HashSet<>();
        allowedDeptIds.add(deptId1);
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

            // 验证：应该只返回技术部的用户
            assertEquals(1, users.size());
            assertEquals(testUserId1, users.get(0).getId());
        } finally {
            DataScopeContext.clear();
        }
    }

    @Test
    @DisplayName("dataScope=2 (本部门及下级): 用户在允许的部门列表中(含子部门)")
    void testDataScopeDeptAndChild() {
        // 设置数据权限上下文：本部门及下级（技术部及其子部门）
        Set<Long> allowedDeptIds = new HashSet<>();
        allowedDeptIds.add(deptId1);
        // 注意：这里应该包含子部门ID，但测试数据中没有子部门，所以只包含技术部
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

            // 验证：应该返回技术部及其子部门的用户
            assertEquals(1, users.size());
            assertEquals(testUserId1, users.get(0).getId());
        } finally {
            DataScopeContext.clear();
        }
    }

    @Test
    @DisplayName("未设置数据权限上下文: 不进行过滤")
    void testNoDataScopeContext() {
        // 不设置数据权限上下文
        DataScopeContext.clear();

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

        // 验证：应该返回所有用户
        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("空部门ID集合: 返回空结果")
    void testEmptyDeptIds() {
        // 设置数据权限上下文：仅本部门，但部门ID集合为空
        DataScopeInfo info = new DataScopeInfo(testUserId1, 3, new HashSet<>());
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

            // 验证：应该返回空结果
            assertEquals(0, users.size());
        } finally {
            DataScopeContext.clear();
        }
    }
}

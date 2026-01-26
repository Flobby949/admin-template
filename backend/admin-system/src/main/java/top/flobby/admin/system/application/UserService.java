package top.flobby.admin.system.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.entity.UserRole;
import top.flobby.admin.system.domain.repository.UserRepository;
import top.flobby.admin.system.domain.repository.UserRoleRepository;
import top.flobby.admin.system.interfaces.dto.UserDTO;
import top.flobby.admin.system.interfaces.query.UserQuery;
import top.flobby.admin.system.interfaces.vo.UserVO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户应用服务
 *
 * @author Flobby
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     */
    public PageResult<UserVO> getUserList(UserQuery query) {
        // 构建分页参数
        Pageable pageable = PageRequest.of(
                query.getPageNum() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        // 查询用户
        Page<User> userPage = userRepository.findByQuery(query, pageable);

        // 转换为VO
        List<UserVO> userVOList = userPage.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(
                userVOList,
                userPage.getTotalElements(),
                (long) query.getPageNum(),
                (long) query.getPageSize()
        );
    }

    /**
     * 根据ID获取用户详情
     */
    public UserVO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToVO(user);
    }

    /**
     * 创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserDTO dto) {
        // 参数校验
        validateUserDTO(dto, true);

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(dto.getPhone()) && userRepository.existsByPhone(dto.getPhone())) {
            throw new BusinessException("手机号已被使用");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus());

        // 保存用户
        User savedUser = userRepository.save(user);

        // 保存用户角色关联
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            saveUserRoles(savedUser.getId(), dto.getRoleIds());
        }

        log.info("创建用户成功，用户ID: {}, 用户名: {}", savedUser.getId(), savedUser.getUsername());
        return savedUser.getId();
    }

    /**
     * 更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO dto) {
        // 参数校验
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 查询用户
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查是否是管理员账号
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能修改管理员账号");
        }

        // 检查用户名是否已被其他用户使用
        if (!user.getUsername().equals(dto.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(dto.getUsername());
        }

        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new BusinessException("邮箱已被使用");
            }
            user.setEmail(dto.getEmail());
        }

        // 检查手机号是否已被其他用户使用
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(dto.getPhone())) {
                throw new BusinessException("手机号已被使用");
            }
            user.setPhone(dto.getPhone());
        }

        // 更新用户信息
        user.setRealName(dto.getRealName());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus());

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 保存用户
        userRepository.save(user);

        // 更新用户角色关联
        if (dto.getRoleIds() != null) {
            // 删除旧的角色关联
            userRoleRepository.deleteByUserId(user.getId());
            // 保存新的角色关联
            if (!dto.getRoleIds().isEmpty()) {
                saveUserRoles(user.getId(), dto.getRoleIds());
            }
        }

        log.info("更新用户成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
    }

    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 查询用户
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查是否是管理员账号
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除管理员账号");
        }

        // 逻辑删除用户
        userRepository.deleteById(id);

        // 删除用户角色关联
        userRoleRepository.deleteByUserId(id);

        log.info("删除用户成功，用户ID: {}, 用户名: {}", id, user.getUsername());
    }

    /**
     * 批量删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        for (Long id : ids) {
            deleteUser(id);
        }
    }

    /**
     * 重置用户密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        // 查询用户
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 校验新密码
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("密码长度必须在6-20个字符之间");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("重置用户密码成功，用户ID: {}, 用户名: {}", id, user.getUsername());
    }

    /**
     * 修改用户状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        // 查询用户
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查是否是管理员账号
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能禁用管理员账号");
        }

        // 校验状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（禁用）或1（启用）");
        }

        // 更新状态
        user.setStatus(status);
        userRepository.save(user);

        log.info("修改用户状态成功，用户ID: {}, 用户名: {}, 新状态: {}", id, user.getUsername(), status);
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        List<UserRole> userRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        }
        userRoleRepository.saveAll(userRoles);
    }

    /**
     * 校验用户DTO
     */
    private void validateUserDTO(UserDTO dto, boolean isCreate) {
        if (isCreate && !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setCreateBy(user.getCreateBy());
        vo.setUpdateBy(user.getUpdateBy());

        // TODO: 加载用户角色和部门信息
        vo.setRoles(new ArrayList<>());
        vo.setDepts(new ArrayList<>());

        return vo;
    }
}

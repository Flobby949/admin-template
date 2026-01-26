package top.flobby.admin.system.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.flobby.admin.system.domain.entity.Role;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.repository.RoleRepository;
import top.flobby.admin.system.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }

        // 加载用户角色
        List<Role> roles = roleRepository.findByUserId(user.getId());

        // 转换为 Spring Security 的权限集合
        // 注意：Spring Security 的 hasRole() 会自动添加 "ROLE_" 前缀
        // 所以我们需要添加 "ROLE_" 前缀，或者使用 hasAuthority()
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode().toUpperCase()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}

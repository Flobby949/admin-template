package top.flobby.admin.system.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.flobby.admin.system.domain.entity.Menu;
import top.flobby.admin.system.domain.entity.Role;
import top.flobby.admin.system.domain.repository.MenuRepository;
import top.flobby.admin.system.domain.repository.RoleRepository;
import top.flobby.admin.system.domain.repository.UserRepository;
import top.flobby.admin.system.domain.entity.User;

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
    private final MenuRepository menuRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }

        // 加载用户角色
        List<Role> roles = roleRepository.findByUserId(user.getId());
        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());

        // 构建权限集合
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 添加角色权限（ROLE_前缀）
        roles.forEach(role ->
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode().toUpperCase()))
        );

        // 加载菜单权限
        if (!roleIds.isEmpty()) {
            List<Menu> menus = menuRepository.findByRoleIds(roleIds);
            menus.stream()
                    .filter(menu -> StringUtils.hasText(menu.getPermission()))
                    .forEach(menu ->
                        authorities.add(new SimpleGrantedAuthority(menu.getPermission()))
                    );
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}

package top.flobby.admin.system.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "用户名称")
    private String name;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "过期时间戳")
    private Long expiresIn;
}

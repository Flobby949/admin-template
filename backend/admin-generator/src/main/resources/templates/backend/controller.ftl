package ${packageName}.${moduleName}.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ${packageName}.common.annotation.RequiresPermission;
import ${packageName}.common.core.Result;
import ${packageName}.${moduleName}.application.${entity.className}Service;
import ${packageName}.${moduleName}.interfaces.dto.${entity.className}DTO;
import ${packageName}.${moduleName}.interfaces.vo.${entity.className}VO;

import java.util.List;

/**
 * ${entity.comment!entity.className}管理控制器
 *
 * @author ${author}
 * @date ${date}
 */
@Tag(name = "${entity.comment!entity.className}管理", description = "${entity.comment!entity.className}管理相关接口")
@RestController
@RequestMapping("/api/${moduleName}/${entity.apiPath}s")
@RequiredArgsConstructor
public class ${entity.className}Controller {

    private final ${entity.className}Service ${entity.classNameLower}Service;

    @Operation(summary = "获取${entity.comment!entity.className}列表")
    @GetMapping
    @RequiresPermission("${entity.permissionPrefix}:list")
    public Result<List<${entity.className}VO>> list() {
        List<${entity.className}VO> list = ${entity.classNameLower}Service.list();
        return Result.success(list);
    }

    @Operation(summary = "获取${entity.comment!entity.className}详情")
    @GetMapping("/{id}")
    @RequiresPermission("${entity.permissionPrefix}:list")
    public Result<${entity.className}VO> getById(
            @Parameter(description = "主键ID") @PathVariable("id") ${entity.primaryKeyType} id) {
        ${entity.className}VO vo = ${entity.classNameLower}Service.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建${entity.comment!entity.className}")
    @PostMapping
    @RequiresPermission("${entity.permissionPrefix}:add")
    public Result<${entity.primaryKeyType}> create(@Valid @RequestBody ${entity.className}DTO dto) {
        ${entity.primaryKeyType} id = ${entity.classNameLower}Service.create(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新${entity.comment!entity.className}")
    @PutMapping("/{id}")
    @RequiresPermission("${entity.permissionPrefix}:edit")
    public Result<Void> update(
            @Parameter(description = "主键ID") @PathVariable("id") ${entity.primaryKeyType} id,
            @Valid @RequestBody ${entity.className}DTO dto) {
        dto.setId(id);
        ${entity.classNameLower}Service.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除${entity.comment!entity.className}")
    @DeleteMapping("/{id}")
    @RequiresPermission("${entity.permissionPrefix}:delete")
    public Result<Void> delete(
            @Parameter(description = "主键ID") @PathVariable("id") ${entity.primaryKeyType} id) {
        ${entity.classNameLower}Service.delete(id);
        return Result.success();
    }
}

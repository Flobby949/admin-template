package top.flobby.admin.system.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.common.annotation.RequiresPermission;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.system.application.RegionService;
import top.flobby.admin.system.interfaces.dto.RegionDTO;
import top.flobby.admin.system.interfaces.vo.RegionVO;

import java.util.List;

/**
 * 区域表管理控制器
 *
 * @author Code Generator
 * @date 2026-02-01
 */
@Tag(name = "区域表管理", description = "区域表管理相关接口")
@RestController
@RequestMapping("/api/system/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "获取区域表列表")
    @GetMapping
    @RequiresPermission("system:region:list")
    public Result<List<RegionVO>> list() {
        List<RegionVO> list = regionService.list();
        return Result.success(list);
    }

    @Operation(summary = "获取区域表详情")
    @GetMapping("/{id}")
    @RequiresPermission("system:region:list")
    public Result<RegionVO> getById(
            @Parameter(description = "主键ID") @PathVariable("id") Long id) {
        RegionVO vo = regionService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建区域表")
    @PostMapping
    @RequiresPermission("system:region:add")
    public Result<Long> create(@Valid @RequestBody RegionDTO dto) {
        Long id = regionService.create(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新区域表")
    @PutMapping("/{id}")
    @RequiresPermission("system:region:edit")
    public Result<Void> update(
            @Parameter(description = "主键ID") @PathVariable("id") Long id,
            @Valid @RequestBody RegionDTO dto) {
        dto.setId(id);
        regionService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除区域表")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:region:delete")
    public Result<Void> delete(
            @Parameter(description = "主键ID") @PathVariable("id") Long id) {
        regionService.delete(id);
        return Result.success();
    }
}

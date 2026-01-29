package top.flobby.admin.system.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.common.annotation.RequiresPermission;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.system.application.DictService;
import top.flobby.admin.system.interfaces.dto.DictDataDTO;
import top.flobby.admin.system.interfaces.dto.DictTypeDTO;
import top.flobby.admin.system.interfaces.vo.DictDataVO;
import top.flobby.admin.system.interfaces.vo.DictTypeVO;

import java.util.List;

/**
 * 字典管理控制器
 */
@Tag(name = "字典管理", description = "字典管理相关接口")
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ==================== 字典类型 ====================

    @Operation(summary = "获取字典类型列表")
    @GetMapping("/types")
    @RequiresPermission("system:dict:list")
    public Result<PageResult<DictTypeVO>> listTypes(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "字典名称") @RequestParam(required = false) String dictName,
            @Parameter(description = "字典类型") @RequestParam(required = false) String dictType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<DictTypeVO> page = dictService.listDictTypes(pageNum, pageSize, dictName, dictType, status);
        return Result.success(page);
    }

    @Operation(summary = "获取字典类型详情")
    @GetMapping("/types/{id}")
    @RequiresPermission("system:dict:query")
    public Result<DictTypeVO> getType(
            @Parameter(description = "字典类型ID") @PathVariable("id") Long id) {
        DictTypeVO dictType = dictService.getDictType(id);
        return Result.success(dictType);
    }

    @Operation(summary = "创建字典类型")
    @PostMapping("/types")
    @RequiresPermission("system:dict:add")
    public Result<Long> createType(@Valid @RequestBody DictTypeDTO dto) {
        Long id = dictService.createDictType(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/types/{id}")
    @RequiresPermission("system:dict:edit")
    public Result<Void> updateType(
            @Parameter(description = "字典类型ID") @PathVariable("id") Long id,
            @Valid @RequestBody DictTypeDTO dto) {
        dictService.updateDictType(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @RequiresPermission("system:dict:delete")
    public Result<Void> deleteType(
            @Parameter(description = "字典类型ID") @PathVariable("id") Long id) {
        dictService.deleteDictType(id);
        return Result.success();
    }

    // ==================== 字典数据 ====================

    @Operation(summary = "获取字典数据列表（管理端）")
    @GetMapping("/data")
    @RequiresPermission("system:dict:list")
    public Result<List<DictDataVO>> listData(
            @Parameter(description = "字典类型") @RequestParam("dictType") String dictType) {
        List<DictDataVO> dictDataList = dictService.listDictData(dictType);
        return Result.success(dictDataList);
    }

    @Operation(summary = "根据字典类型获取字典数据（公共接口）")
    @GetMapping("/data/type/{dictType}")
    public Result<List<DictDataVO>> listDataByType(
            @Parameter(description = "字典类型") @PathVariable("dictType") String dictType) {
        List<DictDataVO> dictDataList = dictService.listDictDataByType(dictType);
        return Result.success(dictDataList);
    }

    @Operation(summary = "获取字典数据详情")
    @GetMapping("/data/{id}")
    @RequiresPermission("system:dict:query")
    public Result<DictDataVO> getData(
            @Parameter(description = "字典数据ID") @PathVariable("id") Long id) {
        DictDataVO dictData = dictService.getDictData(id);
        return Result.success(dictData);
    }

    @Operation(summary = "创建字典数据")
    @PostMapping("/data")
    @RequiresPermission("system:dict:add")
    public Result<Long> createData(@Valid @RequestBody DictDataDTO dto) {
        Long id = dictService.createDictData(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新字典数据")
    @PutMapping("/data/{id}")
    @RequiresPermission("system:dict:edit")
    public Result<Void> updateData(
            @Parameter(description = "字典数据ID") @PathVariable("id") Long id,
            @Valid @RequestBody DictDataDTO dto) {
        dictService.updateDictData(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    @RequiresPermission("system:dict:delete")
    public Result<Void> deleteData(
            @Parameter(description = "字典数据ID") @PathVariable("id") Long id) {
        dictService.deleteDictData(id);
        return Result.success();
    }
}

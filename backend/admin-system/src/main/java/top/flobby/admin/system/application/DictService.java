package top.flobby.admin.system.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.system.domain.entity.DictData;
import top.flobby.admin.system.domain.entity.DictType;
import top.flobby.admin.system.domain.repository.DictDataRepository;
import top.flobby.admin.system.domain.repository.DictTypeRepository;
import top.flobby.admin.system.interfaces.dto.DictDataDTO;
import top.flobby.admin.system.interfaces.dto.DictTypeDTO;
import top.flobby.admin.system.interfaces.vo.DictDataVO;
import top.flobby.admin.system.interfaces.vo.DictTypeVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictService {

    private final DictTypeRepository dictTypeRepository;
    private final DictDataRepository dictDataRepository;
    private final DictCacheService dictCacheService;

    // ==================== 字典类型 ====================

    /**
     * 获取字典类型列表（分页）
     */
    public PageResult<DictTypeVO> listDictTypes(Long pageNum, Long pageSize,
                                                 String dictName, String dictType, Integer status) {
        List<DictType> allDictTypes = dictTypeRepository.findAll();

        // 过滤
        List<DictType> filteredList = allDictTypes.stream()
                .filter(dt -> dictName == null || dt.getDictName().contains(dictName))
                .filter(dt -> dictType == null || dt.getDictType().contains(dictType))
                .filter(dt -> status == null || dt.getStatus().equals(status))
                .toList();

        // 分页
        long total = filteredList.size();
        int start = (int) ((pageNum - 1) * pageSize);
        int end = Math.min(start + pageSize.intValue(), filteredList.size());

        List<DictTypeVO> pageList = filteredList.subList(start, end).stream()
                .map(this::toDictTypeVO)
                .collect(Collectors.toList());

        return PageResult.build(pageList, total, pageNum, pageSize);
    }

    /**
     * 获取字典类型列表（不分页）
     */
    public List<DictTypeVO> listAllDictTypes() {
        List<DictType> dictTypes = dictTypeRepository.findAll();
        return dictTypes.stream()
                .map(this::toDictTypeVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取字典类型详情
     */
    public DictTypeVO getDictType(Long id) {
        DictType dictType = dictTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字典类型不存在"));
        return toDictTypeVO(dictType);
    }

    /**
     * 创建字典类型
     */
    @Transactional
    public Long createDictType(DictTypeDTO dto) {
        // 检查字典类型是否存在
        if (dictTypeRepository.existsByType(dto.getDictType())) {
            throw new BusinessException("字典类型已存在");
        }

        DictType dictType = new DictType();
        dictType.setDictName(dto.getDictName());
        dictType.setDictType(dto.getDictType());
        dictType.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        dictType.setRemark(dto.getRemark());
        dictType.setDeleted(0);

        DictType savedDictType = dictTypeRepository.save(dictType);

        log.info("创建字典类型成功: id={}, type={}", savedDictType.getId(), savedDictType.getDictType());
        return savedDictType.getId();
    }

    /**
     * 更新字典类型
     */
    @Transactional
    public void updateDictType(Long id, DictTypeDTO dto) {
        DictType dictType = dictTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字典类型不存在"));

        // 检查 dictType 是否被修改（不允许修改）
        if (!dictType.getDictType().equals(dto.getDictType())) {
            throw new BusinessException("字典类型标识不允许修改");
        }

        // 更新字段
        dictType.setDictName(dto.getDictName());
        dictType.setStatus(dto.getStatus());
        dictType.setRemark(dto.getRemark());

        dictTypeRepository.save(dictType);

        // 如果状态变为禁用,清除缓存
        if (dto.getStatus() == 0) {
            dictCacheService.clearDictCache(dictType.getDictType());
        }

        log.info("更新字典类型成功: id={}, type={}", id, dictType.getDictType());
    }

    /**
     * 删除字典类型
     */
    @Transactional
    public void deleteDictType(Long id) {
        DictType dictType = dictTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字典类型不存在"));

        // 检查是否有字典数据引用
        long count = dictDataRepository.countByDictType(dictType.getDictType());
        if (count > 0) {
            throw new BusinessException("该字典类型下存在字典数据,无法删除");
        }

        dictTypeRepository.deleteById(id);
        dictCacheService.clearDictCache(dictType.getDictType());

        log.info("删除字典类型成功: id={}, type={}", id, dictType.getDictType());
    }

    // ==================== 字典数据 ====================

    /**
     * 获取字典数据列表（管理端,包含禁用数据）
     */
    public List<DictDataVO> listDictData(String dictType) {
        List<DictData> dictDataList = dictDataRepository.findByDictType(dictType);
        return dictDataList.stream()
                .map(this::toDictDataVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据字典类型获取字典数据（公共接口,仅启用数据,带缓存）
     */
    public List<DictDataVO> listDictDataByType(String dictType) {
        return dictCacheService.getDictDataByType(dictType);
    }

    /**
     * 获取字典数据详情
     */
    public DictDataVO getDictData(Long id) {
        DictData dictData = dictDataRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字典数据不存在"));
        return toDictDataVO(dictData);
    }

    /**
     * 创建字典数据
     */
    @Transactional
    public Long createDictData(DictDataDTO dto) {
        // 检查字典类型是否存在
        dictTypeRepository.findByType(dto.getDictType())
                .orElseThrow(() -> new BusinessException("字典类型不存在"));

        // 检查字典数据是否存在（dictType + dictValue 唯一）
        if (dictDataRepository.existsByDictTypeAndValue(dto.getDictType(), dto.getDictValue())) {
            throw new BusinessException("字典值已存在");
        }

        DictData dictData = new DictData();
        dictData.setDictType(dto.getDictType());
        dictData.setDictLabel(dto.getDictLabel());
        dictData.setDictValue(dto.getDictValue());
        dictData.setDictSort(dto.getDictSort() != null ? dto.getDictSort() : 0);
        dictData.setCssClass(dto.getCssClass());
        dictData.setListClass(dto.getListClass());
        dictData.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        dictData.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        dictData.setRemark(dto.getRemark());
        dictData.setDeleted(0);

        DictData savedDictData = dictDataRepository.save(dictData);

        // 刷新缓存
        dictCacheService.refreshDictCache(dto.getDictType());

        log.info("创建字典数据成功: id={}, type={}, value={}",
                savedDictData.getId(), savedDictData.getDictType(), savedDictData.getDictValue());
        return savedDictData.getId();
    }

    /**
     * 更新字典数据
     */
    @Transactional
    public void updateDictData(Long id, DictDataDTO dto) {
        DictData dictData = dictDataRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字典数据不存在"));

        // 检查 dictType 是否被修改（不允许修改）
        if (!dictData.getDictType().equals(dto.getDictType())) {
            throw new BusinessException("字典类型不允许修改");
        }

        // 检查 dictValue 唯一性（排除自己）
        if (!dictData.getDictValue().equals(dto.getDictValue())) {
            if (dictDataRepository.existsByDictTypeAndValueAndIdNot(
                    dto.getDictType(), dto.getDictValue(), id)) {
                throw new BusinessException("字典值已存在");
            }
        }

        // 更新字段
        dictData.setDictLabel(dto.getDictLabel());
        dictData.setDictValue(dto.getDictValue());
        dictData.setDictSort(dto.getDictSort());
        dictData.setCssClass(dto.getCssClass());
        dictData.setListClass(dto.getListClass());
        dictData.setIsDefault(dto.getIsDefault());
        dictData.setStatus(dto.getStatus());
        dictData.setRemark(dto.getRemark());

        dictDataRepository.save(dictData);

        // 刷新缓存
        dictCacheService.refreshDictCache(dictData.getDictType());

        log.info("更新字典数据成功: id={}, type={}, value={}",
                id, dictData.getDictType(), dictData.getDictValue());
    }

    /**
     * 删除字典数据
     */
    @Transactional
    public void deleteDictData(Long id) {
        DictData dictData = dictDataRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字典数据不存在"));

        // TODO: 检查是否被其他模块引用（需要实现引用检查器）

        dictDataRepository.deleteById(id);

        // 刷新缓存
        dictCacheService.refreshDictCache(dictData.getDictType());

        log.info("删除字典数据成功: id={}, type={}, value={}",
                id, dictData.getDictType(), dictData.getDictValue());
    }

    // ==================== 转换方法 ====================

    private DictTypeVO toDictTypeVO(DictType dictType) {
        return DictTypeVO.builder()
                .id(dictType.getId())
                .dictName(dictType.getDictName())
                .dictType(dictType.getDictType())
                .status(dictType.getStatus())
                .remark(dictType.getRemark())
                .createTime(dictType.getCreateTime())
                .updateTime(dictType.getUpdateTime())
                .build();
    }

    private DictDataVO toDictDataVO(DictData dictData) {
        return DictDataVO.builder()
                .id(dictData.getId())
                .dictType(dictData.getDictType())
                .dictLabel(dictData.getDictLabel())
                .dictValue(dictData.getDictValue())
                .dictSort(dictData.getDictSort())
                .cssClass(dictData.getCssClass())
                .listClass(dictData.getListClass())
                .isDefault(dictData.getIsDefault())
                .status(dictData.getStatus())
                .remark(dictData.getRemark())
                .createTime(dictData.getCreateTime())
                .updateTime(dictData.getUpdateTime())
                .build();
    }
}

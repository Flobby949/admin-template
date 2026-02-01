package top.flobby.admin.system.application;

import java.time.LocalDateTime;
import top.flobby.admin.system.domain.entity.Region;
import top.flobby.admin.system.domain.repository.RegionRepository;
import top.flobby.admin.system.interfaces.dto.RegionDTO;
import top.flobby.admin.system.interfaces.vo.RegionVO;
import top.flobby.admin.system.interfaces.query.RegionQuery;
import top.flobby.admin.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 区域表服务
 *
 * @author Code Generator
 * @date 2026-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    /**
     * 查询区域表列表
     *
     * @return 区域表列表
     */
    public List<RegionVO> list() {
        List<Region> list = regionRepository.findAll();
        return list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询区域表
     *
     * @param id 主键ID
     * @return 区域表
     */
    public RegionVO getById(Long id) {
        Region entity = regionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("区域表不存在"));
        return toVO(entity);
    }

    /**
     * 创建区域表
     *
     * @param dto 创建参数
     * @return 主键ID
     */
    @Transactional
    public Long create(RegionDTO dto) {
        Region entity = new Region();
        copyProperties(dto, entity);
        entity.setDeleted(0);
        Region saved = regionRepository.save(entity);
        log.info("创建区域表成功: id={}", saved.getId());
        return saved.getId();
    }

    /**
     * 更新区域表
     *
     * @param dto 更新参数
     */
    @Transactional
    public void update(RegionDTO dto) {
        Region entity = regionRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException("区域表不存在"));
        copyProperties(dto, entity);
        regionRepository.save(entity);
        log.info("更新区域表成功: id={}", dto.getId());
    }

    /**
     * 删除区域表
     *
     * @param id 主键ID
     */
    @Transactional
    public void delete(Long id) {
        Region entity = regionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("区域表不存在"));
        entity.setDeleted(1);
        regionRepository.save(entity);
        log.info("删除区域表成功: id={}", id);
    }

    /**
     * 实体转VO
     */
    private RegionVO toVO(Region entity) {
        RegionVO vo = new RegionVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setRegionCode(entity.getRegionCode());
        vo.setParentCode(entity.getParentCode());
        vo.setLevel(entity.getLevel());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setCreateBy(entity.getCreateBy());
        return vo;
    }

    /**
     * DTO属性复制到实体
     */
    private void copyProperties(RegionDTO dto, Region entity) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getRegionCode() != null) {
            entity.setRegionCode(dto.getRegionCode());
        }
        if (dto.getParentCode() != null) {
            entity.setParentCode(dto.getParentCode());
        }
        if (dto.getLevel() != null) {
            entity.setLevel(dto.getLevel());
        }
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }
}

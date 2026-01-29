package top.flobby.admin.system.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.flobby.admin.system.domain.entity.DictData;
import top.flobby.admin.system.domain.repository.DictDataRepository;
import top.flobby.admin.system.interfaces.vo.DictDataVO;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 字典缓存服务
 * <p>
 * 采用 Cache-Aside 模式管理字典数据缓存
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictCacheService {

    private final StringRedisTemplate redisTemplate;
    private final DictDataRepository dictDataRepository;
    private final ObjectMapper objectMapper;

    private static final String DICT_CACHE_PREFIX = "admin:dict:data:";
    private static final long CACHE_EXPIRE_HOURS = 2;

    /**
     * 获取字典数据（带缓存）
     * 仅返回启用的数据,按 sort asc, id asc 排序
     */
    public List<DictDataVO> getDictDataByType(String dictType) {
        String cacheKey = DICT_CACHE_PREFIX + dictType;

        try {
            // 尝试从缓存获取
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<List<DictDataVO>>() {});
            }
        } catch (JsonProcessingException e) {
            log.warn("解析字典缓存失败: dictType={}, error={}", dictType, e.getMessage());
            // 缓存解析失败,降级到数据库查询
        }

        // 从数据库加载
        List<DictDataVO> dictDataList = loadDictDataFromDB(dictType);

        // 写入缓存
        try {
            String jsonData = objectMapper.writeValueAsString(dictDataList);
            redisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("写入字典缓存失败: dictType={}, error={}", dictType, e.getMessage());
        }

        return dictDataList;
    }

    /**
     * 刷新字典缓存
     * 重新从数据库加载并覆盖缓存
     */
    public void refreshDictCache(String dictType) {
        String cacheKey = DICT_CACHE_PREFIX + dictType;

        // 从数据库加载
        List<DictDataVO> dictDataList = loadDictDataFromDB(dictType);

        // 覆盖缓存
        try {
            String jsonData = objectMapper.writeValueAsString(dictDataList);
            redisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("刷新字典缓存成功: dictType={}, count={}", dictType, dictDataList.size());
        } catch (JsonProcessingException e) {
            log.error("刷新字典缓存失败: dictType={}, error={}", dictType, e.getMessage());
        }
    }

    /**
     * 清除字典缓存
     */
    public void clearDictCache(String dictType) {
        String cacheKey = DICT_CACHE_PREFIX + dictType;
        redisTemplate.delete(cacheKey);
        log.info("清除字典缓存: dictType={}", dictType);
    }

    /**
     * 从数据库加载字典数据
     */
    private List<DictDataVO> loadDictDataFromDB(String dictType) {
        List<DictData> dictDataList = dictDataRepository.findEnabledByDictType(dictType);

        return dictDataList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 实体转 VO
     */
    private DictDataVO convertToVO(DictData dictData) {
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

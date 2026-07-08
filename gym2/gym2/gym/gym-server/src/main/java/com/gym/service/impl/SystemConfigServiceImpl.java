package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.entity.SystemConfig;
import com.gym.exception.BusinessException;
import com.gym.mapper.SystemConfigMapper;
import com.gym.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public Map<String, String> getAllAsMap() {
        Map<String, String> map = new HashMap<>();
        for (SystemConfig config : listAll()) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
    }

    @Override
    public List<SystemConfig> listAll() {
        return systemConfigMapper.selectList(null);
    }

    @Override
    public void update(String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException("配置项不存在: " + configKey);
        }
        config.setConfigValue(configValue);
        systemConfigMapper.updateById(config);
    }
}

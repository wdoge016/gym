package com.gym.service;

import com.gym.entity.SystemConfig;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {

    Map<String, String> getAllAsMap();

    List<SystemConfig> listAll();

    void update(String configKey, String configValue);
}

package com.isharpever.common.id.snowflake.provider.impl;

import com.isharpever.common.id.snowflake.manager.IdServiceManager;
import com.isharpever.common.id.snowflake.provider.IdServiceProvider;
import java.util.List;
import javax.annotation.Resource;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

@Service(version="1.0.0")
public class IdServiceProviderImpl implements IdServiceProvider {

    @Resource
    private IdServiceManager idServiceManager;

    @Override
    public Long getId() {
        return idServiceManager.getId();
    }

    @Override
    public List<Long> getIds(int num) {
        return idServiceManager.getIds(num);
    }
}

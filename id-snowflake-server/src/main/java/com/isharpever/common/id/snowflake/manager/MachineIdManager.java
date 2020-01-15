package com.isharpever.common.id.snowflake.manager;

import com.isharpever.tool.utils.NetUtil;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MachineIdManager implements InitializingBean {

    private static final short MAX_MACHINE_ID = (1 << 2) - 1;
    private static volatile short my_machine_id = -1;
    private static final String KEY_GENERATE_MACHINE_ID = "key_generate_machine_id";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void afterPropertiesSet() {
        // 初始化机器id
        getMachineId();
    }

    /**
     * 返回当前机器id
     * @return
     */
    public short getMachineId() {
        if (my_machine_id != -1) {
            return my_machine_id;
        }
        // 先用本地缓存的id,若没有则生成新的并缓存
        // 注意:每次重启后都重新生成新id,恰好可以解决重启期间发生时间回拨的问题
        synchronized (this) {
            if (my_machine_id != -1) {
                return my_machine_id;
            }
            Short newId = generateMachineIdFromRedis();
            if (newId != null) {
                my_machine_id = newId;
                return my_machine_id;
            }
        }
        log.error("--- 生成机器id失败");
        throw new RuntimeException("生成机器id失败");
    }

    private Short generateMachineIdFromRedis() {
        Long newId = stringRedisTemplate.opsForValue().increment(KEY_GENERATE_MACHINE_ID);
        if (newId == null) {
            return null;
        }
        if (newId > MAX_MACHINE_ID) {
            log.error("--- 生成的机器id超过最大值 newId={}", newId);
            synchronized (this) {
                newId = stringRedisTemplate.opsForValue().increment(KEY_GENERATE_MACHINE_ID);
                if (newId > MAX_MACHINE_ID) {
                    newId = 1L;
                    stringRedisTemplate.opsForValue().set(KEY_GENERATE_MACHINE_ID, newId.toString());
                }
            }
        }
        return newId.shortValue();
    }

    private String getHostIp() {
        return NetUtil.getLocalHostAddress();
    }
}

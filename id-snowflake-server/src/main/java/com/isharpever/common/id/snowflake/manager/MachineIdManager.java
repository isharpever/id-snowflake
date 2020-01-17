package com.isharpever.common.id.snowflake.manager;

import com.isharpever.common.id.snowflake.constant.IdConstant;
import com.isharpever.tool.executor.ExecutorServiceUtil;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MachineIdManager implements InitializingBean {

    private static final short MAX_MACHINE_ID = (1 << IdConstant.BITS_OF_MACHINEID) - 1;
    private static volatile short my_machine_id = -1;
    private static final String KEY_GENERATE_MACHINE_ID = "key_generate_machine_id";
    private static final String KEY_VERIFY_CRASH = "ylzx-idservice:key_verify_crash";
    private ScheduledExecutorService scheduledExecutorService = ExecutorServiceUtil
            .buildScheduledThreadPool(1, "MachineIdManager");

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
            log.error("--- 生成的机器id超过最大值,从1重新开始 newId={}", newId);
            synchronized (this) {
                newId = stringRedisTemplate.opsForValue().increment(KEY_GENERATE_MACHINE_ID);
                if (newId > MAX_MACHINE_ID) {
                    newId = 1L;
                    stringRedisTemplate.opsForValue().set(KEY_GENERATE_MACHINE_ID, newId.toString());
                }
            }
        }

        // 因为redis崩溃后至少会丢失一个事件循环内的数据,
        // 若真的发生这种情况,有可能造成获得的机器id重复,因此需要确保该机器id唯一
        ensureMachineIdUnique(newId);

        return newId.shortValue();
    }

    /**
     * 确保机器id唯一
     *
     * 因为redis崩溃后至少会丢失一个事件循环内的数据,
     * 若真的发生这种情况,有可能造成获得的机器id重复,例如:
     * 假设redis当前存储的机器id=x(已持久化),id服务器A启动后利用redis自增获得机器id=x+1,
     * 在x+1被持久化之前redis崩溃,恢复之后redis存储的机器id仍是x,
     * 之后id服务器B启动,利用redis自增获得机器id=x+1,与id服务器A持有的机器id重复.
     *
     * 发生这种问题的根源是,id服务器获得机器id之后的较短时间内redis崩溃(自增后的值没有来得及持久化).
     * 我们可以在自增获得机器id后,立即另外redis里保存一个key,
     * 若不能保存成功,可能是redis崩溃了,那么之前自增的值可能丢失,也就存在机器id重复的风险,需要重新生成机器id;
     * 若能保存成功,说明redis没有立即崩溃(理论上redis崩溃恢复需要一个过程,不可能这么短的时间内崩溃又恢复),
     *  然后1秒之后(异步)再去获取保存的值:
     *  若能取到值,说明这1s内redis也没有发生崩溃,或崩溃后又立即恢复了(假设1s来得及恢复吧;)且没有丢失数据,则目前持有的机器id仍是唯一的;
     *  若取不到值,说明这1s内redis发生了崩溃,并丢失了部分数据,存在机器id重复的风险,需要重新生成机器id;
     *
     * @param newId
     */
    private void ensureMachineIdUnique(Long newId) {
        try {
            stringRedisTemplate.opsForValue()
                    .set(KEY_VERIFY_CRASH, newId.toString(), 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("--- 疑似自增获得机器id后redis崩溃", e);
            throw e;
        }

        // 1s后获取该值
        scheduledExecutorService.schedule(() -> {
            String value = stringRedisTemplate.opsForValue().get(KEY_VERIFY_CRASH);
            if (StringUtils.isBlank(value) || !value.equals(newId.toString())) {
                log.error("--- 疑似自增获得机器id后redis崩溃,尝试重新生成机器id");
                generateMachineIdFromRedis();
            }
        }, 1, TimeUnit.SECONDS);
    }
}

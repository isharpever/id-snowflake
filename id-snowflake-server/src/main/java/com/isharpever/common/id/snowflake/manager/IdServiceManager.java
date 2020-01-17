package com.isharpever.common.id.snowflake.manager;

import com.isharpever.common.id.snowflake.constant.IdConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IdServiceManager {

    private static final int BITS_OF_TIMESTAMP = IdConstant.BITS_OF_TIMESTAMP;
    private static final int BITS_OF_IDC = IdConstant.BITS_OF_IDC;
    private static final int BITS_OF_MACHINEID = IdConstant.BITS_OF_MACHINEID;
    private static final int BITS_OF_SEQUENCEID = IdConstant.BITS_OF_SEQUENCEID;
    private static final long START_TIMESTAMP = IdConstant.START_TIMESTAMP;
    private static final int SLOT_NUM = 128;
    private static final AtomicLongArray SLOTS = new AtomicLongArray(SLOT_NUM);

    @Resource
    private MachineIdManager machineIdManager;

    @Resource
    private DcIdManager dcIdManager;

    public Long getId() {
        while (true) {
            // 时间戳
            long timestamp = System.currentTimeMillis() - START_TIMESTAMP;
            timestamp = timestamp & ((1L << BITS_OF_TIMESTAMP) - 1);

            // 若此时间戳所在slot已有id,且其时间戳大于当前时间戳,说明时间回退,则用此id+1作为新id
            // 否则组合新id并放入slot
            int slot = (int)(timestamp & (SLOT_NUM - 1));
            long preId = SLOTS.get(slot);
            long preTimestamp = preId >> (BITS_OF_MACHINEID + BITS_OF_SEQUENCEID);
            long newId;
            if (preId != 0 && preTimestamp >= timestamp) {
                // 检查sequence id是否已达最大值(避免溢出导致机器id甚至时间戳重复)
                int preSequence = (int)(preId & ((1L << BITS_OF_SEQUENCEID) - 1));
                if (preSequence > (1L << BITS_OF_SEQUENCEID) - 1) {
                    log.warn("--- 该毫秒数下序列号已达最大值 preId={}", preId);
                    continue;
                }
                newId = preId + 1;
            } else {
                short idcId = getIdcId();
                short machineId = getMachineId();
                newId = (timestamp << (BITS_OF_IDC + BITS_OF_MACHINEID + BITS_OF_SEQUENCEID))
                        | (idcId << (BITS_OF_MACHINEID + BITS_OF_SEQUENCEID))
                        | (machineId << BITS_OF_SEQUENCEID);
            }
            if (SLOTS.compareAndSet(slot, preId, preId + 1)) {
                return newId;
            }
        }
    }

    public List<Long> getIds(int num) {
        if (num == 0) {
            return null;
        }
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            result.add(getId());
        }
        return result;
    }

    /**
     * 返回机房id
     * @return
     */
    private short getIdcId() {
        return dcIdManager.getDcId();
    }

    /**
     * 返回机器id
     * @return
     */
    private short getMachineId() {
        return machineIdManager.getMachineId();
    }
}

package com.isharpever.common.id.snowflake.dto;

import com.isharpever.common.id.snowflake.constant.IdConstant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class IdComponent {

    /**
     * 生成id的时间
     */
    private LocalDateTime localDateTime;

    /**
     * 机房id
     */
    private int dcId;

    /**
     * 机器id
     */
    private int machineId;

    /**
     * 序列号
     */
    private int sequence;

    public static IdComponent parse(long id) {
        IdComponent idComponent = new IdComponent();
        long timestamp = id >> (IdConstant.BITS_OF_IDC + IdConstant.BITS_OF_MACHINEID
                + IdConstant.BITS_OF_SEQUENCEID);
        idComponent.localDateTime = LocalDateTime.ofEpochSecond(
                (IdConstant.START_TIMESTAMP + timestamp) / 1000,
                        (int) ((IdConstant.START_TIMESTAMP + timestamp) % 1000) * 1000000,
                        ZoneOffset.ofHours(8));
        idComponent.dcId =
                (int)(id >> (IdConstant.BITS_OF_MACHINEID + IdConstant.BITS_OF_SEQUENCEID))
                        & ((1 << IdConstant.BITS_OF_IDC) - 1);
        idComponent.machineId =
                (int) (id >> IdConstant.BITS_OF_SEQUENCEID)
                        & ((1 << IdConstant.BITS_OF_MACHINEID) - 1);
        idComponent.sequence = (int)id & ((1 << IdConstant.BITS_OF_SEQUENCEID) - 1);
        return idComponent;
    }
}

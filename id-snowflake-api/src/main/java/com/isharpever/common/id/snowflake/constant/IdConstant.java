package com.isharpever.common.id.snowflake.constant;

public abstract class IdConstant {

    /**
     * 时间戳位数
     */
    public static final int BITS_OF_TIMESTAMP = 41;

    /**
     * 机房id位数
     */
    public static final int BITS_OF_IDC = 3;

    /**
     * 机器id位数
     */
    public static final int BITS_OF_MACHINEID = 7;

    /**
     * 序列号位数
     */
    public static final int BITS_OF_SEQUENCEID = 12;

    /**
     * 计算时间戳的起始时间
     * 2020-01-15 00:00:00
     */
    public static final long START_TIMESTAMP = 1579017600000L;
}

package com.isharpever.common.id.snowflake.provider;

import java.util.List;

public interface IdServiceProvider {

    /**
     * 返回一个id
     * @return
     */
    Long getId();

    /**
     * 返回指定数目的id
     * @param num
     * @return
     */
    List<Long> getIds(int num);
}

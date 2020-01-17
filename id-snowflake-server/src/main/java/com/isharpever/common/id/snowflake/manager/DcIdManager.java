package com.isharpever.common.id.snowflake.manager;

import com.isharpever.common.id.snowflake.constant.IdConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 生成机房id
 */
@Component
@Slf4j
public class DcIdManager implements InitializingBean {

    private static final short MAX_MACHINE_ID = (1 << IdConstant.BITS_OF_IDC) - 1;
    private static volatile short my_dc_id = -1;

    @Override
    public void afterPropertiesSet() {
        // 初始化机房id
        getDcId();
    }

    /**
     * 返回当前机房id
     * @return
     */
    public short getDcId() {
        if (my_dc_id != -1) {
            return my_dc_id;
        }
        // 可在应用启动参数中指定
        String dc = System.getProperty("dc");
        if (StringUtils.isNotBlank(dc)) {
            try {
                my_dc_id = Short.parseShort(dc);
            } catch (NumberFormatException e) {
            }
        }
        if (my_dc_id >= 0 && my_dc_id <= MAX_MACHINE_ID) {
            return my_dc_id;
        }
        log.error("--- 生成机房id失败 dc={} my_dc_id={}", dc, my_dc_id);
        throw new RuntimeException("生成机房id失败");
    }
}

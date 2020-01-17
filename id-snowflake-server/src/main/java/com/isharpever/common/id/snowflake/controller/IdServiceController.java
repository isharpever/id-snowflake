package com.isharpever.common.id.snowflake.controller;

import com.isharpever.common.id.snowflake.dto.IdComponent;
import com.isharpever.common.id.snowflake.provider.IdServiceProvider;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/id")
public class IdServiceController {

    @Resource
    private IdServiceProvider idServiceProvider;

    @RequestMapping("/getOne")
    public Long getId() {
        long id = idServiceProvider.getId();
        IdComponent idComponent = IdComponent.parse(id);
        return id;
    }
}

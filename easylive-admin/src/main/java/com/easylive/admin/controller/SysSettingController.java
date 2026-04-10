package com.easylive.admin.controller;

import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.SysSettingDto;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.ResponseVo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@RestController
@RequestMapping("/sys")
public class SysSettingController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/getSetting")
    public ResponseVo getSetting() {
        return getSuccessResponseVo(redisComponent.getSysSetting());
    }

    @RequestMapping("/saveSetting")
    public ResponseVo saveSetting(SysSettingDto sysSettingDto) {
        redisComponent.saveSysSetting(sysSettingDto);
        return getSuccessResponseVo(null);
    }
}

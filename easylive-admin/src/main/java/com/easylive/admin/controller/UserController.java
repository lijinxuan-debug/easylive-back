package com.easylive.admin.controller;

import com.easylive.entity.po.UserInfo;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.service.UserInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@RestController
@RequestMapping("/user")
@Validated
public class UserController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/loadUser")
    public ResponseVo loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("join_time desc");
        return getSuccessResponseVo(userInfoService.findListByPage(userInfoQuery));
    }

    @RequestMapping("/changeStatus")
    public ResponseVo changeStatus(@NotEmpty String userId, @NotNull Integer status) {
        userInfoService.changeStatus(userId, status);
        return getSuccessResponseVo(null);
    }
}

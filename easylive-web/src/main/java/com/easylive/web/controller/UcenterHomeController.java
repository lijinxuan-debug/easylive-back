package com.easylive.web.controller;

import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.UserFocus;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.entity.vo.UserInfoVo;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.VideoOrderTypeEnum;
import com.easylive.service.UserActionService;
import com.easylive.service.UserFocusService;
import com.easylive.service.UserInfoService;
import com.easylive.service.VideoInfoService;
import com.easylive.utils.CopyTools;
import com.easylive.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@RestController
@RequestMapping("/ucenter/home")
@Validated
@Slf4j
public class UcenterHomeController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserActionService userActionService;

    @Resource
    private UserFocusService userFocusService;

    @RequestMapping("/getUserInfo")
    public ResponseVo getUserInfo(@NotEmpty String userId) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        UserInfo userInfo = userInfoService.getUserInfoDetail(userInfoDto == null ? null : userInfoDto.getUserId(), userId);

        UserInfoVo userInfoVo = CopyTools.copy(userInfo, UserInfoVo.class);
        return getSuccessResponseVo(userInfoVo);
    }

    @RequestMapping("/updateUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo updateUserInfo(@NotEmpty @Size(max = 100) String avatar,
                                     @NotEmpty @Size(max = 20) String nickName,
                                     @NotNull Integer sex,
                                     @Size(max = 100) String birthday,
                                     @Size(max = 100) String school,
                                     @Size(max = 300) String noticeInfo,
                                     @Size(max = 80) String personalIntroduction) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userInfoDto.getUserId());
        userInfo.setAvatar(avatar);
        userInfo.setNickName(nickName);
        userInfo.setSex(sex);
        userInfo.setBirthday(birthday);
        userInfo.setSchool(school);
        userInfo.setNoticeInfo(noticeInfo);
        userInfo.setPersonalIntroduction(personalIntroduction);

        userInfoService.updateUserInfo(userInfo, userInfoDto);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/saveTheme")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo saveTheme(@Min(1) @Max(10) @NotNull Integer theme) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.setTheme(theme);
        userInfoService.updateUserInfoByUserId(userInfo, userInfoDto.getUserId());
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/focus")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo focus(@NotEmpty String focusUserId) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        userFocusService.focus(userInfoDto.getUserId(), focusUserId);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/cancelFocus")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo cancelFocus(@NotEmpty String focusUserId) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        userFocusService.cancelFocus(userInfoDto.getUserId(), focusUserId);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadFocusList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadFocusList(Integer pageNo) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setUserId(userInfoDto.getUserId());
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setQueryType(Constants.ZERO);
        userFocusQuery.setOrderBy("u.focus_time desc");
        PaginationResultVo<UserFocus> paginationResultVo = userFocusService.findListByPage(userFocusQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/loadFansList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadFansList(Integer pageNo) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setFocusUserId(userInfoDto.getUserId());
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setQueryType(Constants.ONE);
        userFocusQuery.setOrderBy("u.focus_time desc");
        PaginationResultVo<UserFocus> paginationResultVo = userFocusService.findListByPage(userFocusQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/loadVideoList")
    public ResponseVo loadVideoList(@NotEmpty String userId, Integer type,
                                    Integer pageNo, String videoName, Integer orderType) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if (type != null) {
            videoInfoQuery.setPageSize(PageSizeEnum.SIZE10.getSize());
        }
        VideoOrderTypeEnum videoOrderTypeEnum = VideoOrderTypeEnum.getByType(orderType);
        if (videoOrderTypeEnum == null) {
            videoOrderTypeEnum = VideoOrderTypeEnum.CREATE_TIME;
        }
        videoInfoQuery.setOrderBy(videoOrderTypeEnum.getField() + " desc");
        videoInfoQuery.setVideoNameFuzzy(videoName);
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setPageNo(pageNo);
        PaginationResultVo<VideoInfo> paginationResultVo = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/loadUserCollection")
    public ResponseVo loadUserCollection(@NotEmpty String userId,
                                         Integer pageNo) {
        UserActionQuery userActionQuery = new UserActionQuery();
        userActionQuery.setUserId(userId);
        userActionQuery.setPageNo(pageNo);
        userActionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        userActionQuery.setOrderBy("u.action_time desc");
        userActionQuery.setQueryVideoInfo(true);
        PaginationResultVo<UserAction> paginationResultVo = userActionService.findListByPage(userActionQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

}

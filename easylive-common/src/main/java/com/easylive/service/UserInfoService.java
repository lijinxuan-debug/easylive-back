package com.easylive.service;

import java.util.List;

import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.dto.UserCountInfoDto;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;

import javax.servlet.http.HttpServletResponse;

/**
 * @description: user_info Service
 * @date: 2025-03-02
 */
public interface UserInfoService {
    /**
     * 根据条件查询列表
     */
    List<UserInfo> findListByParam(UserInfoQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(UserInfoQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<UserInfo> findListByPage(UserInfoQuery query);

    /**
     * 新增
     */
    Integer add(UserInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserInfo> listBean);

    /**
     * 批量新增/更新
     */
    Integer addOrUpdateBatch(List<UserInfo> listBean);

    /**
     * 根据UserId查询
     */
    UserInfo getUserInfoByUserId(String userId);

    /**
     * 根据UserId更新
     */
    Integer updateUserInfoByUserId(UserInfo bean, String userId);

    /**
     * 根据UserId删除
     */
    Integer deleteUserInfoByUserId(String userId);

    /**
     * 根据Email查询
     */
    UserInfo getUserInfoByEmail(String email);

    /**
     * 根据Email更新
     */
    Integer updateUserInfoByEmail(UserInfo bean, String email);

    /**
     * 根据Email删除
     */
    Integer deleteUserInfoByEmail(String email);

    /**
     * 根据NickName查询
     */
    UserInfo getUserInfoByNickName(String nickName);

    /**
     * 根据NickName更新
     */
    Integer updateUserInfoByNickName(UserInfo bean, String nickName);

    /**
     * 根据NickName删除
     */
    Integer deleteUserInfoByNickName(String nickName);

    void register(String email, String nickName, String password);

    TokenUserInfoDto login(String email, String password, String ip);

    UserInfo getUserInfoDetail(String currentUserId,String userId);

    void updateUserInfo(UserInfo userInfo, TokenUserInfoDto userInfoDto);

    UserCountInfoDto getCountInfo(String userId);

    void changeStatus(String userId, Integer status);
}
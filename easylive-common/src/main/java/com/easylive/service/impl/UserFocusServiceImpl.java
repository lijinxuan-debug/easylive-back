package com.easylive.service.impl;

import java.util.Date;
import java.util.List;

import com.easylive.entity.po.UserFocus;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.service.UserFocusService;
import com.easylive.mappers.UserFocusMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: user_focus ServiceImpl
 * @date: 2025-03-08
 */
@Service("userFocusService")
public class UserFocusServiceImpl implements UserFocusService {

    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据条件查询列表
     */
    public List<UserFocus> findListByParam(UserFocusQuery query) {
        return userFocusMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(UserFocusQuery query) {
        return userFocusMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<UserFocus> findListByPage(UserFocusQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserFocus> list = this.findListByParam(query);
        PaginationResultVo<UserFocus> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserFocus bean) {
        return userFocusMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserFocus> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userFocusMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<UserFocus> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userFocusMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据UserIdAndFocusUserId查询
     */
    public UserFocus getUserFocusByUserIdAndFocusUserId(String userId, String focusUserId) {
        return userFocusMapper.selectByUserIdAndFocusUserId(userId, focusUserId);
    }

    /**
     * 根据UserIdAndFocusUserId更新
     */
    public Integer updateUserFocusByUserIdAndFocusUserId(UserFocus bean, String userId, String focusUserId) {
        return userFocusMapper.updateByUserIdAndFocusUserId(bean, userId, focusUserId);
    }

    /**
     * 根据UserIdAndFocusUserId删除
     */
    public Integer deleteUserFocusByUserIdAndFocusUserId(String userId, String focusUserId) {
        return userFocusMapper.deleteByUserIdAndFocusUserId(userId, focusUserId);
    }

    @Override
    public void focus(String userId, String focusUserId) {
        if (userId.equals(focusUserId)) {
            throw new BusinessException("不能关注自己");
        }
        UserInfo dbUserInfo = userInfoMapper.selectByUserId(focusUserId);
        if (dbUserInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        UserFocus dbUserFocus = userFocusMapper.selectByUserIdAndFocusUserId(userId, focusUserId);
        if (dbUserFocus != null) {
            return;
        }
        UserFocus focus = new UserFocus();
        focus.setUserId(userId);
        focus.setFocusUserId(focusUserId);
        focus.setFocusTime(new Date());
        userFocusMapper.insert(focus);
    }

    @Override
    public void cancelFocus(String userId, String focusUserId) {
        userFocusMapper.deleteByUserIdAndFocusUserId(userId, focusUserId);
    }
}
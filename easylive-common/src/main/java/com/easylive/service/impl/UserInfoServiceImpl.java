package com.easylive.service.impl;

import java.util.Date;
import java.util.List;

import com.easylive.component.RedisComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.CountInfoDto;
import com.easylive.entity.dto.SysSettingDto;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.dto.UserCountInfoDto;
import com.easylive.entity.po.UserFocus;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.enums.UserStatusEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserFocusMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.UserInfoService;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.utils.CopyTools;
import com.easylive.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description: user_info ServiceImpl
 * @date: 2025-03-02
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    /**
     * 根据条件查询列表
     */
    public List<UserInfo> findListByParam(UserInfoQuery query) {
        return userInfoMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(UserInfoQuery query) {
        return userInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<UserInfo> findListByPage(UserInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(query);
        PaginationResultVo<UserInfo> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserInfo bean) {
        return userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserId查询
     */
    public UserInfo getUserInfoByUserId(String userId) {
        return userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId更新
     */
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    public Integer deleteUserInfoByUserId(String userId) {
        return userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email查询
     */
    public UserInfo getUserInfoByEmail(String email) {
        return userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email更新
     */
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    public Integer deleteUserInfoByEmail(String email) {
        return userInfoMapper.deleteByEmail(email);
    }

    /**
     * 根据NickName查询
     */
    public UserInfo getUserInfoByNickName(String nickName) {
        return userInfoMapper.selectByNickName(nickName);
    }

    /**
     * 根据NickName更新
     */
    public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
        return userInfoMapper.updateByNickName(bean, nickName);
    }

    /**
     * 根据NickName删除
     */
    public Integer deleteUserInfoByNickName(String nickName) {
        return userInfoMapper.deleteByNickName(nickName);
    }

    @Override
    public void register(String email, String nickName, String password) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (userInfo != null) {
            throw new BusinessException("该邮箱已被注册");
        }
        userInfo = userInfoMapper.selectByNickName(nickName);
        if (userInfo != null) {
            throw new BusinessException("该昵称已被使用");
        }
        userInfo = new UserInfo();
        String userId = StringTools.getRandomNumbers(Constants.LENGTH_10);
        userInfo.setUserId(userId);
        userInfo.setEmail(email);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringTools.passwordMD5(password));
        userInfo.setJoinTime(new Date());
        userInfo.setStatus(UserStatusEnum.ENABLE.getCode());
        userInfo.setTheme(Constants.ONE);
        userInfo.setPersonalIntroduction(Constants.DEFAULT_PERSONAL_INTRODUCTION);
        userInfo.setSchool(Constants.DEFAULT_SCHOOL);
        userInfo.setBirthday("");

        SysSettingDto sysSetting = redisComponent.getSysSetting();
        userInfo.setCurrentCoinCount(sysSetting.getRegisterCoinCount());
        userInfo.setTotalCoinCount(sysSetting.getRegisterCoinCount());
        userInfoMapper.insert(userInfo);
    }

    @Override
    public TokenUserInfoDto login(String email, String password, String ip) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (userInfo == null || !password.equals(userInfo.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (UserStatusEnum.DISABLE.getCode().equals(userInfo.getStatus())) {
            throw new BusinessException("用户已被禁用");
        }
        UserInfo updateUserInfo = new UserInfo();
        // 更新Ip和最后一次登陆时间
        updateUserInfo.setLastLoginTime(new Date());
        updateUserInfo.setLastLoginIp(ip);
        userInfoMapper.updateByUserId(updateUserInfo, userInfo.getUserId());

        TokenUserInfoDto tokenUserInfoDto = CopyTools.copy(userInfo, TokenUserInfoDto.class);
        // 重新生成token并存放到redis里面
        redisComponent.saveTokenInfo4Web(tokenUserInfoDto);
        return tokenUserInfoDto;
    }

    @Override
    public UserInfo getUserInfoDetail(String currentUserId, String userId) {
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        if (userInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_404);
        }
        CountInfoDto countInfo = videoInfoMapper.selectSumCountInfo(userId);
        userInfo.setPlayCount(countInfo.getPlayCount());
        userInfo.setLikeCount(countInfo.getLikeCount());
        Integer fansCount = userFocusMapper.selectFansCount(userId);
        Integer focusCount = userFocusMapper.selectFocusCount(userId);
        userInfo.setFansCount(fansCount);
        userInfo.setFocusCount(focusCount);

        //关注状态
        if (currentUserId == null) { //未登录，均显示未关注
            userInfo.setHaveFocus(false);
        } else {
            UserFocus userFocus = userFocusMapper.selectByUserIdAndFocusUserId(currentUserId, userId);
            userInfo.setHaveFocus(userFocus != null);
        }
        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfo userInfo, TokenUserInfoDto userInfoDto) {
        UserInfo dbInfo = userInfoMapper.selectByUserId(userInfo.getUserId());
        if (!dbInfo.getNickName().equals(userInfo.getNickName()) && dbInfo.getCurrentCoinCount() < Constants.UPDATE_USER_NICK_NAME_COIN) {
            throw new BusinessException("硬币不足，无法修改昵称");
        }

        if (!dbInfo.getNickName().equals(userInfo.getNickName())) {
            Integer count = userInfoMapper.updateCoinCount(dbInfo.getUserId(), -Constants.UPDATE_USER_NICK_NAME_COIN);
            if (count == 0) {
                throw new BusinessException("硬币不足，无法修改昵称");
            }
        }
        userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());

        boolean updateTokenInfo = false;
        if (!userInfo.getAvatar().equals(userInfoDto.getAvatar())) {
            userInfoDto.setAvatar(userInfo.getAvatar());
            updateTokenInfo = true;
        }
        if (!userInfo.getNickName().equals(userInfoDto.getNickName())) {
            userInfoDto.setNickName(userInfo.getNickName());
            updateTokenInfo = true;
        }
        if (updateTokenInfo) {
            redisComponent.updateTokenInfo(userInfoDto);
        }
    }

    @Override
    public UserCountInfoDto getCountInfo(String userId) {
        UserCountInfoDto userCountInfoDto = new UserCountInfoDto();
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        Integer focusCount = userFocusMapper.selectFocusCount(userId);
        Integer fansCount = userFocusMapper.selectFansCount(userId);
        userCountInfoDto.setFocusCount(focusCount);
        userCountInfoDto.setFansCount(fansCount);
        userCountInfoDto.setCurrentCoinCount(userInfo.getCurrentCoinCount());
        return userCountInfoDto;
    }

    @Override
    public void changeStatus(String userId, Integer status) {
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setStatus(status);
        userInfoMapper.updateByUserId(updateUserInfo, userId);
    }
}
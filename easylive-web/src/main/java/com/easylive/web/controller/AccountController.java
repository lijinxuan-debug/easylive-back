package com.easylive.web.controller;

import com.easylive.component.RedisComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.dto.UserCountInfoDto;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.exception.BusinessException;
import com.easylive.service.UserInfoService;
import com.easylive.utils.StringTools;
import com.easylive.web.annotation.GlobalInterceptor;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: user_info Controller
 * @date: 2025-03-02
 */
@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/checkCode")
    public ResponseVo checkCode() {
        // 生成图片验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        // 获取结果
        String checkCode = captcha.text();
        // 保存到redis 并返回用户标识
        String checkCodeKey = redisComponent.saveCheckCode(checkCode);
        // 将图片验证码base64返回前端
        String checkCodeBase64 = captcha.toBase64();

        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVo(result);
    }

    @RequestMapping("/register")
    public ResponseVo register(@NotEmpty @Email @Size(max = 150) String email,
                               @NotEmpty @Size(max = 30) String nickName,
                               @NotEmpty @Pattern(regexp = Constants.PASSWORD_REGEX) String registerPassword,
                               @NotEmpty String checkCode,
                               @NotEmpty String checkCodeKey) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            userInfoService.register(email, nickName, registerPassword);
            return getSuccessResponseVo(null);
        } finally {
            // 清除redis中的验证码
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping("/login")
    public ResponseVo login(HttpServletRequest request,
                            HttpServletResponse response,
                            @NotEmpty @Email String email,
                            @NotEmpty String password,
                            @NotEmpty String checkCode,
                            @NotEmpty String checkCodeKey) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            String ip = getIpAddr();
            // 将token存放到redis
            TokenUserInfoDto tokenUserInfoDto = userInfoService.login(email, password, ip);
            // 将token存放到cookie
            saveToken2Cookie(response, tokenUserInfoDto.getToken());
            return getSuccessResponseVo(tokenUserInfoDto);
        } finally {
            // 清除redis中的验证码
            redisComponent.cleanCheckCode(checkCodeKey);
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                String token = null;
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(Constants.WEB_TOKEN)) {
                        token = cookie.getValue();
                        break;
                    }
                }
                if (!StringTools.isEmpty(token)) {
                    redisComponent.cleanTokenInfo4Web(token);
                }
            }
        }
    }

    @RequestMapping("/autoLogin")
    public ResponseVo autoLogin(HttpServletResponse response) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVo(null);
        }
        if (tokenUserInfoDto.getExpireTime() - System.currentTimeMillis() < Constants.REDIS_KEY_EXPIRE_ONE_DAY) {
            redisComponent.saveTokenInfo4Web(tokenUserInfoDto);
            saveToken2Cookie(response, tokenUserInfoDto.getToken());
        }
        return getSuccessResponseVo(tokenUserInfoDto);
    }

    @RequestMapping("/loginOut")
    public ResponseVo loginOut(HttpServletResponse response) {
        cleanCookie(response);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/getCountInfo")
    public ResponseVo getCountInfo() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        UserCountInfoDto userCountInfoDto = userInfoService.getCountInfo(tokenUserInfoDto.getUserId());
        return getSuccessResponseVo(userCountInfoDto);
    }
}
package com.easylive.admin.controller;

import com.easylive.component.RedisComponent;
import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.exception.BusinessException;
import com.easylive.service.UserInfoService;
import com.easylive.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @PostMapping("/checkCode")
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

    @RequestMapping("/login")
    public ResponseVo login(HttpServletRequest request,
                            HttpServletResponse response,
                            @NotEmpty String account,
                            @NotEmpty String password,
                            @NotEmpty String checkCode,
                            @NotEmpty String checkCodeKey) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            if (!appConfig.getAdminAccount().equals(account) || !password.equals(StringTools.passwordMD5(appConfig.getAdminPassword()))) {
                throw new BusinessException("用户名或密码错误");
            }
            String token = redisComponent.saveTokenInfo4Admin(account);
            saveToken2Cookie(response, token);
            return getSuccessResponseVo(account);
        } finally {
            // 清除redis中的验证码
            redisComponent.cleanCheckCode(checkCodeKey);
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                String token = null;
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(Constants.ADMIN_TOKEN)) {
                        token = cookie.getValue();
                        break;
                    }
                }
                if (!StringTools.isEmpty(token)) {
                    redisComponent.cleanTokenInfo4Admin(token);
                }
            }
        }
    }

    @RequestMapping("/loginOut")
    public ResponseVo loginOut(HttpServletResponse response) {
        cleanCookie(response);
        return getSuccessResponseVo(null);
    }
}
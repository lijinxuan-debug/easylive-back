package com.easylive.admin.interceptor;

import com.easylive.component.RedisComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.enums.ResponseEnum;
import com.easylive.exception.BusinessException;
import com.easylive.utils.StringTools;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * 全局拦截器
 * 拦截除/account和/file外的所有请求，检查用户Token，未登录则抛出异常。
 */
@Component
public class AppInterceptor implements HandlerInterceptor {

    private static final String URL_ACCOUNT = "/account";
    private static final String URL_FILE = "/file";

    @Resource
    private RedisComponent redisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (null == handler) {
            return false;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 如果是登陆注册服务则直接返回true
        if (request.getRequestURI().contains(URL_ACCOUNT)) {
            return true;
        }
        //普通API请求：从HTTP头ADMIN_TOKEN获取Token。
        String token = request.getHeader(Constants.ADMIN_TOKEN);
        //文件资源请求：从CookieWEB_TOKEN获取Token。
        if (request.getRequestURI().contains(URL_FILE)) {
            token = getTokenFromCookie(request);
        }
        if (StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseEnum.CODE_901);
        }
        //通过Redis验证Token有效性，无效或缺失则拒绝访问。
        Object sessionObj = redisComponent.getTokenInfo4Admin(token);
        if (null == sessionObj) {
            throw new BusinessException(ResponseEnum.CODE_901);
        }
        return true;
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.ADMIN_TOKEN)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
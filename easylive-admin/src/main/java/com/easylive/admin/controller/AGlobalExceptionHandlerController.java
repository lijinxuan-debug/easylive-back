package com.easylive.admin.controller;

import com.easylive.entity.vo.ResponseVo;
import com.easylive.enums.ResponseEnum;
import com.easylive.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.net.BindException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class AGlobalExceptionHandlerController extends ABaseController {
    private static final Logger logger = LoggerFactory.getLogger(AGlobalExceptionHandlerController.class);

    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        logger.error("请求错误，请求地址:{}，异常信息:", request.getRequestURI(), e);
        ResponseVo ajaxResponse = new ResponseVo();
        // 这里可以根据不同的异常类型，返回不同的错误信息
        if (e instanceof NoHandlerFoundException) {
            ajaxResponse.setCode(ResponseEnum.CODE_404.getCode());
            ajaxResponse.setMessage(ResponseEnum.CODE_404.getMsg());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof BusinessException) {
            //业务异常
            BusinessException businessException = (BusinessException) e;
            ajaxResponse.setCode(businessException.getCode() == null ? ResponseEnum.CODE_600.getCode() : businessException.getCode());
            ajaxResponse.setMessage(businessException.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof BindException || e instanceof MethodArgumentNotValidException) {
            //参数类型错误
            ajaxResponse.setCode(ResponseEnum.CODE_600.getCode());
            ajaxResponse.setMessage(ResponseEnum.CODE_600.getMsg());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof DuplicateKeyException) {
            //主键冲突
            ajaxResponse.setCode(ResponseEnum.CODE_601.getCode());
            ajaxResponse.setMessage(ResponseEnum.CODE_601.getMsg());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof ConstraintViolationException) {
            ajaxResponse.setCode(ResponseEnum.CODE_600.getCode());
            ajaxResponse.setMessage(ResponseEnum.CODE_600.getMsg());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else {
            //未知异常
            ajaxResponse.setCode(ResponseEnum.CODE_500.getCode());
            ajaxResponse.setMessage(ResponseEnum.CODE_500.getMsg());
            ajaxResponse.setStatus(STATUS_ERROR);
        }
        return ajaxResponse;
    }
}

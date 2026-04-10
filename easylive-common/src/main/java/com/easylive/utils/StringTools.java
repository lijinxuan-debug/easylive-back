package com.easylive.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @projectName: easybbs
 * @author: Li
 * @description: 字符串工具类
 */

public class StringTools {

    // 判断字符串是否为空
    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str.trim()) || "null".equals(str)) {
            return true;
        } else {
            return false;
        }
    }

    // 按照指定长度生成随机字符串(包含大小写字母和数字)
    public static String getRandomStringAndNumbers(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    // 按照指定长度生成随机字符串(只包含数字)
    public static String getRandomNumbers(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    // 按照指定长度生成随机字符串(只包含字母)
    public static String getRandomLetters(Integer count) {
        return RandomStringUtils.random(count, true, false);
    }

    // 密码加密
    public static String passwordMD5(String password) {
        return StringTools.isEmpty(password) ? null : DigestUtils.md5Hex(password);
    }

    //获取文件名后缀
    public static String getFileSuffix(String fileName) {
        if (StringTools.isEmpty(fileName) || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    //获取文件名
    public static String getFileName(String fileName) {
        if (StringTools.isEmpty(fileName) || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    //判断路径是否安全
    public static boolean pathIsOk(String path) {
        if (StringTools.isEmpty(path)) {
            return false;
        }
        if (path.contains("../") || path.contains("..\\")) {
            return false;
        }
        return true;
    }

    // 首字母大写
    public static String upperCaseFirstLetter(String field) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }
}

package com.yu.util;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author yu
 * @DateTime 2020/8/15 23:42
 */
public class RequestUtil {

    public static String getUserInfo(HttpServletRequest request){
        String remoteHost = request.getRemoteHost();
        String remoteUser = request.getRemoteUser();
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr + ":" + remoteHost + ":" + remoteUser;
    }
}

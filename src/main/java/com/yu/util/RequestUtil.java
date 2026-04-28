package com.yu.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class RequestUtil {

    public static String getUserInfo(HttpServletRequest request) {
        String remoteHost = request.getRemoteHost();
        String remoteUser = request.getRemoteUser();
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr + ":" + remoteHost + ":" + remoteUser;
    }

    public static String getDecodeQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (StringUtils.hasLength(queryString)) {
            try {
                queryString = URLDecoder.decode(queryString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
        } else {
            queryString = "";
        }
        return queryString;
    }
}

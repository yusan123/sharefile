package com.yu.interceptor;

import com.yu.util.RequestUtil;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;


/**
 * @Author yu
 * @DateTime 2020/8/15 23:10
 */
//TODO 使用lombok来记录日志
public class OperationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //用来记录哪个用户做了什么操作
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        //如果是上传操作则记录上传信息
        if(request instanceof StandardMultipartHttpServletRequest){
            StandardMultipartHttpServletRequest r = (StandardMultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> multiFileMap = r.getMultiFileMap();
            List<MultipartFile> files = multiFileMap.get("files");

            List<String> fileNames = files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList());

            LOGGER.info(String.format("来自%s的%s上传请求,上传%s个文件为:%s", RequestUtil.getUserInfo(request), uri, files.size(),fileNames));
        }else{
            LOGGER.info(String.format("来自%s的%s请求,参数为:%s", RequestUtil.getUserInfo(request), uri, queryString));
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}

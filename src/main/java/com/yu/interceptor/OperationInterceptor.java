package com.yu.interceptor;

import com.yu.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OperationInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (request instanceof StandardMultipartHttpServletRequest) {
            StandardMultipartHttpServletRequest r = (StandardMultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> multiFileMap = r.getMultiFileMap();
            if (multiFileMap != null) {
                List<MultipartFile> files = multiFileMap.get("files");
                if (files != null && !files.isEmpty()) {
                    List<String> fileNames = files.stream()
                            .map(MultipartFile::getOriginalFilename)
                            .collect(Collectors.toList());
                    LOGGER.info("来自{}的{}上传请求,上传{}个文件:{}", RequestUtil.getUserInfo(request), uri, files.size(), fileNames);
                }
            }
        } else {
            LOGGER.info("来自{}的{}请求,参数:{}", RequestUtil.getUserInfo(request), uri, RequestUtil.getDecodeQueryString(request));
        }
        return true;
    }
}

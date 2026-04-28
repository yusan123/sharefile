package com.yu.exception;

import com.yu.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExeHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExeHandler.class);

    @ExceptionHandler(Exception.class)
    public String exeHandler(HttpServletRequest request, Exception e, Model model) {
        model.addAttribute("errMsg", e.getMessage());
        String url = request.getRequestURL().toString();
        LOGGER.error("出错:来自:{},请求url:{},参数:{},错误信息:{}",
                RequestUtil.getUserInfo(request), url, RequestUtil.getDecodeQueryString(request), e.getMessage());
        if (!(e instanceof ShareFileException)) {
            e.printStackTrace();
        }
        return "error";
    }
}

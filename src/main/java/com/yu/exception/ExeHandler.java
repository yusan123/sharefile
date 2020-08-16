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
        LOGGER.error(String.format("出错:来自:%s,请求url:%s,参数为:%s,错误信息:%s", RequestUtil.getUserInfo(request),
                url, RequestUtil.getDecodeQueryString(request), e.getMessage()));
        //只在发生非自定义异常时打印堆栈方便定位问题
        if (!(e instanceof ShareFileException)) {
            e.printStackTrace();
        }
        return "error";
    }
}

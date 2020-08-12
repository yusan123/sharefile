package com.yu.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MyExceptionAdvice implements ErrorController {
    @Autowired
    HttpServletRequest request;


    @Override
    @RequestMapping("/error")
    public String getErrorPath() {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        switch (statusCode) {
            case 404:
                return "/404";
            case 400:
                return "/400";
            default:
                return "/500";
        }
    }

}

package com.yu.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyExceptionAdvice implements ErrorController {

    @Override
    @RequestMapping("/error")
    public String getErrorPath() {
        return "404";
    }
}

package com.yu.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExeHandler {

    @ExceptionHandler
    public String exeHandler(Exception e, Model model){
        model.addAttribute("errMsg", e.getMessage());
        e.printStackTrace();
        return "error";
    }
}

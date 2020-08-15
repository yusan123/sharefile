package com.yu.config;

import com.yu.interceptor.OperationInterceptor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author yu
 * @DateTime 2020/8/15 23:27
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(new OperationInterceptor());
        registration.addPathPatterns("/**");
        registration.excludePathPatterns("/","/favicon.ico","/error");
    }
}

package com.yu.config;

import com.yu.interceptor.OperationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private OperationInterceptor operationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/favicon.ico", "/error");
    }
}

package com.ouweihao.community.config;

import com.ouweihao.community.controller.interceptor.AlphaInterceptor;
import com.ouweihao.community.controller.interceptor.DateCountInterceptor;
import com.ouweihao.community.controller.interceptor.LoginTicketInterceptor;
import com.ouweihao.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DateCountInterceptor dateCountInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("**/*.css", "**/*.js", "**/*.png", "**/*.jpg", "**/*.jpeg")
                .addPathPatterns("/register", "/login");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("**/*.css", "**/*.js", "**/*.png", "**/*.jpg", "**/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("**/*.css", "**/*.js", "**/*.png", "**/*.jpg", "**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("**/*.css", "**/*.js", "**/*.png", "**/*.jpg", "**/*.jpeg");

        registry.addInterceptor(dateCountInterceptor)
                .excludePathPatterns("**/*.css", "**/*.js", "**/*.png", "**/*.jpg", "**/*.jpeg");

    }

    /**
     * 添加静态资源，外部可以直接访问地址
     * @param registry
     */
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/public/**").addResourceLocations("classpath:/static/images/");
//    }
}

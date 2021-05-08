package com.ouweihao.community.controller.interceptor;

import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.DateCountService;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DateCountInterceptor implements HandlerInterceptor {

    @Autowired
    private DateCountService dateCountService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String ip = request.getRemoteHost();
        dateCountService.recordUV(ip);

        User currentUser = hostHolder.getUser();
        if (currentUser != null) {
            dateCountService.recordDAU(currentUser.getId());
        }

        return true;
    }
}

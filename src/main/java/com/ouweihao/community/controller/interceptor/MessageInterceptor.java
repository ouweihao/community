package com.ouweihao.community.controller.interceptor;

import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.MessageService;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User currentUser = hostHolder.getUser();

        if (currentUser != null && modelAndView != null) {
            int unreadLetterCount = messageService.findUnreadLetterCount(currentUser.getId(), null);
            int unreadNoticeCount = messageService.findUnreadNoticeCount(currentUser.getId(), null);

            int allUnreadCount = unreadLetterCount + unreadNoticeCount;
            modelAndView.addObject("allUnreadCount", allUnreadCount);
        }

    }
}

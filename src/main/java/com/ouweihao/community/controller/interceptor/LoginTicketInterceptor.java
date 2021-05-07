package com.ouweihao.community.controller.interceptor;

import com.ouweihao.community.entity.LoginTicket;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.CookieUtil;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从session中取到ticket
        String ticket = CookieUtil.getValue(request, "ticket");

        // 若ticket不为空则进行以下逻辑
        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检验凭证是否有效，第三个是用于判断失效时间是否晚于现在的时间
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次中持有用户
                hostHolder.setUser(user);

                // 构建用户认证的结果，并存入SecurityContext中，以便于Security进行授权
                Collection<? extends GrantedAuthority> authorities = userService.getAuthorities(user.getId());
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), authorities);

                // 摄者SecurityHostHolder的内容
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }

        return true;
    }

    /**
     * 在模板引擎前将User存入Model
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 从请求中获得登录凭证对应的用户
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    /**
     * 在模板引擎渲染之后销毁user
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 销毁user
        hostHolder.clear();
        // 清除权限
        SecurityContextHolder.clearContext();
    }
}

package com.ouweihao.community.config;

import com.ouweihao.community.util.CommunityConstant;
import com.ouweihao.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 静态资源全能访问
        web.ignoring().antMatchers("resource/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_USER
                )
                .anyRequest().permitAll()
                .and().csrf().disable();

        // 权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 没有登录时的处理
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        // 得到请求方式，如果是XMLHttpRequest的话，就返回JSON格式的字符串
                        String xRequestWith = request.getHeader("x-request-with");
                        if ("XMLHttpRequest".equals(xRequestWith)) {
                            // 是通过异步的方式请求，返回JSON格式的字符串
                            response.setContentType("application/plain;charset=utf-8");
                            Writer writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403, "你还没登录！！"));
                        } else {
                            // 直接重定向到登录页面
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不够时的处理
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        // 得到请求方式，如果是XMLHttpRequest的话，就返回JSON格式的字符串
                        String xRequestWith = request.getHeader("x-request-with");
                        if ("XMLHttpRequest".equals(xRequestWith)) {
                            // 是通过异步的方式请求，返回JSON格式的字符串
                            response.setContentType("application/plain;charset=utf-8");
                            Writer writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403, "你没有访问此功能的权限！！"));
                        } else {
                            // 直接重定向到登录页面
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        // Security底层默认会拦截/logout请求，进行退出处理
        // 所以我们要覆盖他的默认逻辑，才能执行我们自己的退出代码
        // 这里个securityLogout在系统中并没有对应的url，我们只是让他不处理我们所写的退出url
        http.logout().logoutUrl("/securityLogout");

    }
}

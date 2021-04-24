package com.ouweihao.community.controller.advice;

import com.ouweihao.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    // 配置日志

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handlException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 打印日志
        logger.error("服务器发生异常：" + e.getMessage());

        // 循环打印错误信息
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        // 获得请求方式，如果是XMLHttpRequest的话就是异步请求，需要返回JSON格式的字符串
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // 也可以使用application/JSON;charset=utf-8
            // plain代表传入的可以使普通字符串，可以使JSON格式化的，或者在后面再手动格式化
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJsonString(1, "服务器异常!!"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

}

package com.ouweihao.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // 开启定时任务
@EnableAsync // 让被注解的方法能在多线程的环境下，被异步的调用
public class ThreadPoolConfig {
}

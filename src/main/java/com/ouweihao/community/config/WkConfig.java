package com.ouweihao.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * 因为本类是个配置类，所以Spring容器在创建时
     * 会实例化本类，而这个方法是在实例化之后执行
     */
    @PostConstruct
    public void init() {
        // 创建Wk图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            LOGGER.info("创建WK图片目录：" + wkImageStorage);
        }
    }

}

package com.ouweihao.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket docket(Environment env){
        Profiles profiles = Profiles.of("test", "dev");
        boolean flag = env.acceptsProfiles(profiles);
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(true)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ouweihao.community.controller"))
//                .paths(PathSelectors.ant(""))
                .build();
    }

    public ApiInfo apiInfo(){
        Contact contact = new Contact("欧伟豪", "http://8.129.233.237:8080/", "1804400792@qq.com");
        return new ApiInfo(
                "Api Documentation",
                "Api Documentation",
                "v1.0",
                "http://8.129.233.237:8080/",
                contact,
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList()
        );
    }

}

package pers.cy.speedkillsystem.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

// 配置注解
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    // 注入自己编写的userArgumentResolver
    @Autowired
    private UserArgumentResolver userArgumentResolver;


    /**
     * spring mvc的controller中的方法参数都是框架调用这个方法来进行赋值的，自动根据controller方法中的参数进行赋值。他会遍历controller方法中的参数，如果有这个类型的参数，就会直接进行自动赋值
     * 自定义这个功能就需要自己编写参数解析器，然后在这个方法中将自己编写的参数解析器注册
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // 注册SksUser参数解析器注册
        argumentResolvers.add(userArgumentResolver);
    }
}

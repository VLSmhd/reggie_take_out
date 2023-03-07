package com.vls.reggie.config;

import com.vls.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    /**
     * 设置静态资源映射
     * 翻译：添加资源处理程序
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("静态资源映射：");
        registry.addResourceHandler("/backend/**")//根据网页的前端请求来写,实际含义就是：处理资源请求
                .addResourceLocations("classpath:/backend/");//classpath对应resource目录

        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }


    /**
     * 拓展MVC的消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器，页面上的JSON数据就是通过转换器转换的
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将java对象转化为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //消息转换器追加到MVC框架的转换器容器中
        converters.add(0,messageConverter);//这个0是索引，目的是让我们的转换器优先级最高,然后其它转换器不会失效
    }
}

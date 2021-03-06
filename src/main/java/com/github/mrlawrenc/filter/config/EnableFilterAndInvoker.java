package com.github.mrlawrenc.filter.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 可以通过此注解导入相关的配置了到容器（需要有{@link Import}注解）,或者通过配置spring.factory文件来引入相关配置
 * @author hz20035009-逍遥
 * date   2020/5/27 18:02
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan("com.github.mrlawrenc")
@Import({RegisterConfig.class, FilterAutoConfig.class})
public @interface EnableFilterAndInvoker {
}

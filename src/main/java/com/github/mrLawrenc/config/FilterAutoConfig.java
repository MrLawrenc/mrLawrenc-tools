package com.github.mrLawrenc.config;

import com.github.mrLawrenc.filter.FilterChain;
import com.github.mrLawrenc.filter.impl.FirstFilter;
import com.github.mrLawrenc.filter.impl.LastFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : hz20035009-逍遥
 * @date : 2020/5/19 15:21
 * @description : TODO
 */
@Configuration
@ConditionalOnClass({FilterChain.class, FirstFilter.class, LastFilter.class})
@EnableConfigurationProperties(Config.class)
public class FilterAutoConfig {

    @Bean
    public FilterChain filterChain() {
        return new FilterChain();
    }

    @Bean
    public FirstFilter firstFilter() {
        return new FirstFilter();
    }

    @Bean
    public LastFilter lastFilter() {
        return new LastFilter();
    }

    @Bean
    public InjectProxyBeanProcessor injectProxyBeanProcessor(FilterChain filterChain) {
        return new InjectProxyBeanProcessor(filterChain);
    }
}
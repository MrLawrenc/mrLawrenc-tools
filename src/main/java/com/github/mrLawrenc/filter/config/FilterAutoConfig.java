package com.github.mrLawrenc.filter.config;

import com.github.mrLawrenc.filter.service.FilterChain;
import com.github.mrLawrenc.filter.service.FirstFilter;
import com.github.mrLawrenc.filter.service.LastFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:02
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
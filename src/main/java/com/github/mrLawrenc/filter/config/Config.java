package com.github.mrLawrenc.filter.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : hz20035009-逍遥
 * @date : 2020/5/18 15:20
 * @description : 配置相关信息
 */
@Data
@ConfigurationProperties
public class Config {
    @Value("${filter.basepkg: }")
    private String basePkg;
}
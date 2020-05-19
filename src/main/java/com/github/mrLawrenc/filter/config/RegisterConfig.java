package com.github.mrLawrenc.filter.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author : MrLawrenc
 * @date : 2020/5/13 23:33
 * @description : 可以通过registry实现手动向容器注入bean
 */
public class RegisterConfig implements ImportBeanDefinitionRegistrar {
    public static BeanDefinitionRegistry registry;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        RegisterConfig.registry = registry;
    }
}
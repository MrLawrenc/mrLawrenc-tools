package com.github.mrLawrenc.filter.impl;


import com.github.mrLawrenc.config.Config;
import com.github.mrLawrenc.filter.FilterChain;
import com.github.mrLawrenc.filter.Request;
import com.github.mrLawrenc.filter.Response;
import com.github.mrLawrenc.filter.standard.InnerFilter;

/**
 * @author : hz20035009-逍遥
 * @date : 2020/5/14 9:42
 * @description : 对于入站来说是第一个filter，对于出站则是最后一个filter
 * <p>
 * 留给子类扩展，默认空实现
 */
public class FirstFilter extends InnerFilter {
    @Override
    public void init(Config filterConfig) {
        System.out.println("first init.......");
    }

    @Override
    public FilterChain doFilter(Request request, Response response, FilterChain chain) {
        System.out.println("first do.......");
        return chain;
    }

    @Override
    public void destroy() {

    }
}
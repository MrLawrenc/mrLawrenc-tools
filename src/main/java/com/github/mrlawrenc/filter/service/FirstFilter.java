package com.github.mrlawrenc.filter.service;


import com.github.mrlawrenc.filter.config.Config;
import com.github.mrlawrenc.filter.entity.Request;
import com.github.mrlawrenc.filter.entity.Response;
import com.github.mrlawrenc.filter.standard.InnerFilter;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:04
 * 对于入站来说是第一个filter，对于出站则是最后一个filter
 *  * <p>
 *  * 留给子类扩展，默认空实现
 */
public class FirstFilter extends InnerFilter {
    @Override
    public void init(Config filterConfig) {
        System.out.println("first init.......");
    }

    @Override
    public FilterChain doFilter(Request request, Response response, FilterChain chain) {
        // sub do
        return chain;
    }

    @Override
    public void destroy() {

    }
}
package com.github.mrLawrenc.filter.service;


import com.github.mrLawrenc.filter.config.Config;
import com.github.mrLawrenc.filter.entity.Request;
import com.github.mrLawrenc.filter.entity.Response;
import com.github.mrLawrenc.filter.standard.InnerFilter;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:04
 * 对于出站来说是第一个filter，对于入站则是最后一个filter
 *  * <p>
 *  * 留给子类扩展，默认空实现
 */
public class LastFilter extends InnerFilter {
    @Override
    public void init(Config filterConfig) {
        System.out.println("last init.......");
    }

    @Override
    public FilterChain doFilter(Request request, Response response, FilterChain chain) {
        System.out.println("last do.......");
        return chain;
    }

    @Override
    public void destroy() {

    }
}
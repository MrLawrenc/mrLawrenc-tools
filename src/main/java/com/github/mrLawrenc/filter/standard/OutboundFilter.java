package com.github.mrLawrenc.filter.standard;


import com.github.mrLawrenc.filter.service.FilterChain;
import com.github.mrLawrenc.filter.entity.Request;
import com.github.mrLawrenc.filter.entity.Response;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:05
 * 出站过滤器
 */
public abstract class OutboundFilter implements Filter {
    @Override
    public FilterChain doFilter(Request request, Response response, FilterChain chain) {
        return doOutboundFilter(response, chain);
    }

    public abstract FilterChain doOutboundFilter(Response response, FilterChain chain);
}
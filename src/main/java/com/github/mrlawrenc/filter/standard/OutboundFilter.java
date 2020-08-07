package com.github.mrlawrenc.filter.standard;


import com.github.mrlawrenc.filter.service.FilterChain;
import com.github.mrlawrenc.filter.entity.Request;
import com.github.mrlawrenc.filter.entity.Response;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:05
 * 出站过滤器
 */
public abstract class OutboundFilter<T extends Request, R extends Response> implements Filter<T, R> {
    @Override
    public final FilterChain doFilter(T request, R response, FilterChain chain) {
        return doOutboundFilter(response, chain);
    }

    public abstract FilterChain doOutboundFilter(R response, FilterChain chain);
}
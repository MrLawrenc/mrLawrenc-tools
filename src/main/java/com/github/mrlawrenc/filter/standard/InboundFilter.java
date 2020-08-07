package com.github.mrlawrenc.filter.standard;


import com.github.mrlawrenc.filter.service.FilterChain;
import com.github.mrlawrenc.filter.entity.Request;
import com.github.mrlawrenc.filter.entity.Response;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:05
 * 入站过滤器
 */
public abstract class InboundFilter<T extends Request, R extends Response> implements Filter<T, R> {
    /**
     * 尽量不要复写该方法，可以使用{@link InboundFilter#doInboundFilter(Request, FilterChain)}
     *
     * @param request  请求数据
     * @param response 响应数据
     * @param chain    过滤器链
     */
    @Override
    public final FilterChain doFilter(T request, R response, FilterChain chain) {
        return doInboundFilter(request, chain);
    }

    /**
     * 具体的执行过滤的方法
     *
     * @param request 请求
     * @param chain   过滤器链
     * @return 过滤器链
     */
    public abstract FilterChain doInboundFilter(T request, FilterChain chain);
}
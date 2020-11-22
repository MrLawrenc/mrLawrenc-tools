package com.github.mrlawrenc.filter.service;

import com.alibaba.fastjson.JSON;
import com.github.mrlawrenc.filter.config.Config;
import com.github.mrlawrenc.filter.config.RegisterConfig;
import com.github.mrlawrenc.filter.entity.Request;
import com.github.mrlawrenc.filter.entity.Response;
import com.github.mrlawrenc.filter.standard.Filter;
import com.github.mrlawrenc.filter.standard.InboundFilter;
import com.github.mrlawrenc.filter.standard.OutboundFilter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:04
 * 过滤器链
 * * <p>
 * * first和last filter是一定会被执行的
 */
@Slf4j
public class FilterChain implements InitializingBean {

    @Autowired
    private ApplicationContext context;


    @Autowired
    private Config config;

    /**
     * 所有的filter合集，包括first和last
     */
    private List<Filter> allFilter;

    /**
     * 所有非first和last过滤器
     */
    @Getter
    private List<Filter> beanFilters;


    private List<FirstFilter> firstFilters;
    private List<LastFilter> lastFilters;


    /**
     * 内部调用执行所有的过滤器
     * 使用者可以在firstFilter更改整个过滤器链，详见{@link FilterChain#reloadFilters(List)}方法
     *
     * @param method      源方法
     * @param methodProxy 代理方法
     * @param objects     方法参数
     * @param proxyObj    代理对象
     * @return 响应
     * @throws Throwable 异常
     */
    public Response doFilter(Object proxyObj, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Request request = (Request) objects[0];

        FilterChain chain = this;
        for (FirstFilter firstFilter : firstFilters) {
            chain = firstFilter.doFilter(request, null, chain);
        }
        inbound(0, request, chain);
        final FilterChain finalChain = chain;
        lastFilters.forEach(lastFilter -> lastFilter.doFilter(request, null, finalChain));


        //Response response = (Response) methodProxy.invokeSuper(proxyObj, objects);
        //此时 proxyObj为原始对象，才能使用自动注入的字段
        Response response = (Response) method.invoke(proxyObj, objects);


        lastFilters.forEach(lastFilter -> lastFilter.doFilter(request, null, finalChain));
        outbound(allFilter.size() - 1, response, finalChain);
        firstFilters.forEach(firstFilter -> firstFilter.doFilter(request, response, finalChain));

        return response;
    }

    /**
     * 移除所有入栈filter，进而直接到达业务处理Invoker
     *
     * @return 新的构建的过滤器链
     */
    public FilterChain skip2Service() {
        FilterChain filterChain = copy();
        filterChain.beanFilters = filterChain.beanFilters.stream().filter(f -> f instanceof InboundFilter).collect(toList());
        return filterChain;
    }

    /**
     * 返回一个chain副本
     *
     * @return chain
     */
    public FilterChain copy() {
        FilterChain filterChain = new FilterChain();
        filterChain.beanFilters = new ArrayList<>(this.beanFilters.size());
        filterChain.firstFilters = new ArrayList<>(this.firstFilters.size());
        filterChain.lastFilters = new ArrayList<>(this.lastFilters.size());
        filterChain.allFilter = new ArrayList<>(this.allFilter.size());

        Collections.copy(filterChain.beanFilters, this.beanFilters);
        Collections.copy(filterChain.firstFilters, this.firstFilters);
        Collections.copy(filterChain.lastFilters, this.lastFilters);
        Collections.copy(filterChain.allFilter, this.allFilter);
        return filterChain;
    }

    /**
     * 依次执行所有入站处理器
     *
     * @param current 当前是第几个过滤器
     * @param request 请求
     * @param chain   过滤器链
     */
    private void inbound(int current, Request request, FilterChain chain) {
        if (current < beanFilters.size()) {
            Filter filter = beanFilters.get(current++);
            if (filter instanceof InboundFilter) {
                InboundFilter inboundFilter = (InboundFilter) filter;
                chain = inboundFilter.doInboundFilter(request, chain);

            }
            chain.inbound(current, request, chain);
        }
    }

    /**
     * 依次执行所有出站过滤器
     *
     * @param current  当前是第几个过滤器
     * @param response 源响应
     * @param chain    过滤器链
     */
    private void outbound(int current, Response response, FilterChain chain) {
        if (current >= 0) {
            Filter filter = allFilter.get(current--);
            if (filter instanceof OutboundFilter) {
                OutboundFilter outboundFilter = (OutboundFilter) filter;
                chain = outboundFilter.doOutboundFilter(response, chain);
            }
            chain.outbound(current, response, chain);
        }
    }

    /**
     * 初始化当前环境中所有filter
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        String basePkg = config.getBasePkg().trim();
        beanFilters = new ArrayList<>(context.getBeansOfType(Filter.class).values());

        if (!StringUtils.isEmpty(basePkg)) {
            log.info("开始扫描自定义包:{}下的Filter实现类", basePkg);
            Reflections reflections = new Reflections(basePkg);
            Set<Class<? extends Filter>> subClz = reflections.getSubTypesOf(Filter.class);

            List<? extends Class<? extends Filter>> sourceClz = beanFilters.stream().map(Filter::getClass).collect(toList());

            List<Class<? extends Filter>> newBeanFilter = subClz.stream().filter(c -> !sourceClz.contains(c)
                    && !Modifier.isAbstract(c.getModifiers())).peek(result -> {
                RootBeanDefinition definition = new RootBeanDefinition();
                definition.setBeanClass(result);
                RegisterConfig.registry.registerBeanDefinition(result.getSimpleName().toLowerCase(), definition);
                beanFilters.add(context.getBean(result));
            }).collect(toList());

            log.info("add bean : {}", newBeanFilter);
        }

        firstFilters = beanFilters.stream().filter(f -> f instanceof FirstFilter).map(f -> (FirstFilter) f).collect(toList());
        lastFilters = beanFilters.stream().filter(f -> f instanceof LastFilter).map(f -> (LastFilter) f).collect(toList());
        beanFilters.removeAll(firstFilters);
        beanFilters.removeAll(lastFilters);

        //排序
        AnnotationAwareOrderComparator.sort(beanFilters);

        //first全部添加到入站头，last添加到出站头
        AnnotationAwareOrderComparator.sort(firstFilters);
        AnnotationAwareOrderComparator.sort(lastFilters);


        //整合所有filter
        allFilter = new ArrayList<>(firstFilters.size() + beanFilters.size() + lastFilters.size());
        allFilter.addAll(firstFilters);
        allFilter.addAll(beanFilters);
        allFilter.addAll(lastFilters);

        CompletableFuture.runAsync(() -> allFilter.forEach(filter -> filter.init(config)));
    }


    /**
     * 一般在用户自定义的{@link FirstFilter#doFilter(Request, Response, FilterChain)}被调用
     * <p>
     * 允许用户在特殊条件更改本次执行的过滤器链，更改只会在本次请求生效，如果想要复用，就不能调用{@link FilterChain#clearChain()}方法，
     * 并且需要保存当前FilterChain副本，以便下次在进入{@link FirstFilter#doFilter(Request, Response, FilterChain)}方法时更改当前的filterChain
     * <p>
     * 重新装配filter,不会修改原有的first和last类型的过滤器。
     *
     * @param filterList 新的过滤器链
     * @return this
     */
    public FilterChain reloadFilters(List<Filter> filterList) {
        FilterChain filterChain = JSON.parseObject(JSON.toJSONString(this), FilterChain.class);
        filterChain.beanFilters = filterList;
        allFilter.clear();
        allFilter.addAll(firstFilters);
        allFilter.addAll(beanFilters);
        allFilter.addAll(lastFilters);
        return filterChain;
    }

    /**
     * 清理过滤器链副本数据,通常在用户自定义的{@link LastFilter#doFilter(Request, Response, FilterChain)}执行
     */
    public void clearChain() {
        this.beanFilters.clear();
        this.firstFilters.clear();
        this.lastFilters.clear();
    }
}
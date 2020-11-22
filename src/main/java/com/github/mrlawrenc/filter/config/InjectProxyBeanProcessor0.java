package com.github.mrlawrenc.filter.config;


import com.github.mrlawrenc.filter.service.FilterChain;
import com.github.mrlawrenc.filter.standard.Invoker;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.core.NamingPolicy;
import org.springframework.cglib.core.Predicate;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:03
 */
@AllArgsConstructor
public class InjectProxyBeanProcessor0 implements BeanPostProcessor {
    /*
     * {@link com.github.mrlawrenc.filter.config.InjectProxyBeanProcessor}该类由于作用于初始化之前，因此会出现原始bean未实例化，
     * 因此也就没有注入相关字段依赖。
     * 同时注意在使用cglib中，调用源方法时，使用methodProxy.invokeSuper(proxyObj, objects);在使用自动注入的字段时会报错空指针
     * <code>
     * <pre>
     * {@code
     *     Enhancer enhancer = new Enhancer();
     *     enhancer.setSuperclass(invoker.getClass());
     *     enhancer.setNamingPolicy(new NamingPolicy() {
     *        @Override
     *         public String getClassName(String s, String s1, Object o, Predicate predicate) {
     *             return "Proxy$" + invoker.getClass().getSimpleName() + COUNT.getAndIncrement();
     *         }
     *     });
     *     enhancer.setCallback(new MethodInterceptor() {
     *         @Override
     *         public Object intercept(Object proxyObj, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
     *             return methodProxy.invokeSuper(proxyObj, objects);
     *         }
     *     });
     *     enhancer.create();}
     * </pre>
     * </code>
     * 详见如下使用方法
     * 更多 创建动态代理Bean : https://www.cnblogs.com/hujunzheng/p/10463798.html
     */
    private static final AtomicInteger COUNT = new AtomicInteger();
    private final FilterChain filterChain;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //此处在所有的invoker实现类bean实例化（区分初始化）之前执行
        if (Invoker.class.isAssignableFrom(bean.getClass())) {
            return proxyInvoker(bean);
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    /**
     * 为所有的Invoker对象生成代理对象，在执行{@link Invoker}的doInvoke(Request)方法
     * 前、后进行过滤器的调用
     */
    private <T extends Invoker> T proxyInvoker(Object bean) {

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bean.getClass());
        enhancer.setNamingPolicy(new NamingPolicy() {
            @Override
            public String getClassName(String s, String s1, Object o, Predicate predicate) {
                return "Proxy$" + bean.getClass().getSimpleName() + COUNT.getAndIncrement();
            }
        });
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object proxyObj, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                /*
                 * 其实这种方式创建的代理Bean使用问题的，@Autowired字段没有注入进来，所以会有出现NPE。
                 * methodProxy.invokeSuper(target, args)，这一行代码是有问题的，target是代理类对象，
                 * 而真实的对象是postProcessBeforeInitialization(Object bean, String beanName) 中的bean对象，
                 * 此时bean对象@Autowired字段已经注入了。所以可以将methodProxy.invokeSuper(target, args)
                 * 修改为method.invoke(bean, args)解决无法注入@Autowired字段的问题。
                 */
                //不使用代理对象，而使用源bean对象（包含注入的字段）
                if (method.getName().equals("doInvoke")) {
                    return filterChain.doFilter(bean, method, objects, methodProxy);
                } else {
                    return method.invoke(bean, objects);
                }
            }
        });
        return (T) enhancer.create();
    }
}
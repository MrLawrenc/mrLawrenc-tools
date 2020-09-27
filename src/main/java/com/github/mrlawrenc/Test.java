package com.github.mrlawrenc;

import com.github.mrlawrenc.filter.config.Config;
import com.github.mrlawrenc.filter.config.EnableFilterAndInvoker;
import com.github.mrlawrenc.filter.entity.Request;
import com.github.mrlawrenc.filter.entity.Response;
import com.github.mrlawrenc.filter.service.FilterChain;
import com.github.mrlawrenc.filter.standard.InboundFilter;
import com.github.mrlawrenc.filter.standard.Invoker;
import com.github.mrlawrenc.filter.standard.OutboundFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author MrLawrenc
 * date  2020/8/4
 *
 *
 */
@SpringBootApplication
@EnableFilterAndInvoker
public class Test implements ApplicationRunner {

    @Autowired
    private List<Invoker> context;

    public static void main(String[] args) {
        SpringApplication.run(Test.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(context.get(0).doInvoke(new Request()));
    }


    @Component
    @Order(5)
    public static class AFilter extends InboundFilter {

        @Override
        public FilterChain doInboundFilter(Request request, FilterChain chain) {
            System.out.println("AFilter");
            return chain;
        }

        @Override
        public void init(Config filterConfig) {

        }

        @Override
        public void destroy() {

        }
    }

    @Component
    @Order(6)
    public static class A1Filter extends InboundFilter {

        @Override
        public FilterChain doInboundFilter(Request request, FilterChain chain) {
            System.out.println("A1Filter");
            return chain;
        }

        @Override
        public void init(Config filterConfig) {

        }

        @Override
        public void destroy() {

        }
    }

    @Component
    public static class BFilter extends OutboundFilter {


        @Override
        public void init(Config filterConfig) {

        }

        @Override
        public void destroy() {

        }

        @Override
        public FilterChain doOutboundFilter(Response response, FilterChain chain) {
            System.out.println("BFilter");
            return chain;
        }
    }

    @Component
    public static class A implements Invoker {

        @Autowired
        private ApplicationContext context;

        @Override
        public Response doInvoke(Request request) {
            System.out.println(context);
            return null;
        }
    }
}

package com.github.mrlawrenc.filter.standard;


import com.github.mrlawrenc.filter.entity.Request;
import com.github.mrlawrenc.filter.entity.Response;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:05
 * 业务逻辑接口
 */
public interface Invoker<T extends Request, R extends Response> {


    /**
     * 业务方法
     *
     * @param request 请求
     * @return 响应
     */
    R doInvoke(T request);
}

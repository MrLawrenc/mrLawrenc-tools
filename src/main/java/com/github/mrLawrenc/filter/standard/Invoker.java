package com.github.mrLawrenc.filter.standard;


import com.github.mrLawrenc.filter.entity.Request;
import com.github.mrLawrenc.filter.entity.Response;

/**
 * @author : MrLawrenc
 * @date : 2020/5/13 22:43
 * @description :   业务逻辑接口
 */
public interface Invoker {


    /**
     * 业务方法
     *
     * @param request 请求
     * @return 响应
     */
    Response doInvoke(Request request);
}

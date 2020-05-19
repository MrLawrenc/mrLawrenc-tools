package com.github.mrLawrenc.filter.entity;

import lombok.Data;

/**
 * @author : MrLawrenc
 * @date : 2020/5/13 22:36
 * @description : 请求对象
 */
@Data
public class Request {

    /**
     * 具体业务数据
     */
    private Object data;

}
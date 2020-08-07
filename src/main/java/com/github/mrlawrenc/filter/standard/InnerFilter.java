package com.github.mrlawrenc.filter.standard;

import com.github.mrlawrenc.filter.entity.Request;
import com.github.mrlawrenc.filter.entity.Response;

/**
 * @author hz20035009-逍遥
 * date   2020/5/27 18:02
 */
public abstract class InnerFilter<T extends Request,R extends Response> implements Filter<T,R> {
}
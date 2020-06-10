package com.github.mrlawrenc.cache;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author hz20035009-逍遥
 * date  2020/5/26 9:42
 * 序列化缓存
 */
public interface Cache<T> {

    boolean save(String key, T data);

    CompletableFuture<?> asyncSave(String key, T data);

    List<T> list(String key);

    List<T> asyncList(String key);
}

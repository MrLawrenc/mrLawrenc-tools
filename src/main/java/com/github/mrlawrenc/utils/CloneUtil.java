package com.github.mrlawrenc.utils;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.util.List;

/**
 * 拷贝工具类
 *
 * @author MrLawrenc
 * date  2020/5/27 23:48
 */
@SuppressWarnings("all")
public class CloneUtil {

    /**
     * json序列化方式深拷贝
     *
     * @param <T>    目标对象类型
     * @param clz    目标对象类型
     * @param source 源对象
     * @return 拷贝之后的对象
     */
    public static <T> T deepCopyObj(Object source, Class<T> clz) {
        return JSON.parseObject(JSON.toJSONString(source), clz);
    }

    public static <T> List<T> deepCopyList(List<T> source, Class<T> clz) {
        return JSON.parseArray(JSON.toJSONString(source), clz);
    }

    /**
     * 基于jdk序列化方式的深克隆
     *
     * @param <L>    源类型
     * @param source 被复制拷贝的源对象
     * @return 拷贝之后的对象
     * @throws Exception 序列化错误
     */
    public static <L> L deepCopyBySerialized(L source) throws Exception{
        if (!(source instanceof Serializable)) {
            throw new RuntimeException("Not Impl Serialized Interface");
        }
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bao);
        oo.writeObject(source);
        ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bai);
        return (L) oi.readObject();
    }

}
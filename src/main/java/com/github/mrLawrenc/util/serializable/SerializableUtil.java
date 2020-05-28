package com.github.mrLawrenc.util.serializable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author hz20035009-逍遥
 * date  2020/5/26 14:19
 * Xstream序列化框架工具类
 * 值得注意的是：该序列化方式，序列化lambda对象，只需要最外层的对象实现类序列化接口即可（如SerializableRunnable），
 * 内部依赖的对象不实现序列化接口也可以序列化
 * <br>
 * xstream版本禁止 : <=1.4.6或=1.4.10
 * @see https://www.mi1k7ea.com/2019/10/21/XStream%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E6%BC%8F%E6%B4%9E/
 */
@SuppressWarnings("all")
public class SerializableUtil {


    private static XStream xStream;

    static {
        xStream = new XStream(new DomDriver());
        /*
         * 使用xStream.alias(String name, Class Type)为任何一个自定义类创建到类到元素的别名
         * 如果不使用别名，则生成的标签名为类全名
         * xStream.alias("person", Person.class);
         */

    }

    /**
     * xml转java对象
     */
    public static <T> T xmlToBean(String xml) {
        return (T) xStream.fromXML(xml);
    }

    /**
     * java对象转xml
     */
    public static String beanToXml(Object obj) {
        return xStream.toXML(obj);
    }

}
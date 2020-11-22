package com.github.mrlawrenc.utils.serializable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author hz20035009-逍遥
 * date  2020/5/26 14:19
 * Xstream序列化框架工具类
 * 值得注意的是 该序列化方式，序列化lambda对象，只需要最外层的对象实现类序列化接口即可（如SerializableRunnable），
 * 内部依赖的对象不实现序列化接口也可以序列化
 * <br>
 * <pre>
 *     xstream版本禁止  小于1.4.6或等于1.4.10
 *     xstream漏洞  https://www.mi1k7ea.com/2019/10/21/XStream%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E6%BC%8F%E6%B4%9E/
 * </pre>
 */
@SuppressWarnings("all")
public class SerializableUtil {


    private static XStream xStream;

    static {
        xStream = new XStream(new DomDriver());
    }

    /**
     * xml转java对象
     *
     * @param xml 序列化之后的xml
     * @param <T> bean类型
     * @return 反序列化之后的bean
     */
    public static <T> T xmlToBean(String xml) {
        return (T) xStream.fromXML(xml);
    }

    /**
     * java对象转xml
     *
     * @param obj 需要序列化的对象
     * @return 序列化之后的xml
     */
    public static String beanToXml(Object obj) {
        return xStream.toXML(obj);
    }

}
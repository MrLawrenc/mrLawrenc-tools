package com.github.mrLawrenc.util;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 类相关工具
 *
 * @author MrLawrenc
 * date  2020/5/27 23:50
 */
public class ClzUtil {


    /**
     * 会在当前target/classes目录下生成java文件和class文件
     *
     * @param clzName 类名
     * @param file    java文件
     * @return 类加载之后反射生成的对象
     */
    public static Object generateJavaFile(String clzName, File file) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int compilationResult = compiler.run(null, null, null, file.getAbsolutePath());
        if (compilationResult == 0) {
            try {
                //2.获取URL对象
                URL url = file.toURI().toURL();

                //3.创建URL类加载器
                URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});

                //4.通过urlClassLoader加载器调用loadClass方法传入类名动态加载class文件并获取class对象:会初始化静态块
                Class<?> targetClz = urlClassLoader.loadClass(clzName);

                //5.通过class对象创建实例
                assert targetClz != null;
                return targetClz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
package com.github.mrlawrenc.utils;

import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类相关工具
 *
 * @author MrLawrenc
 * date  2020/5/27 23:50
 */
@Slf4j
@SuppressWarnings("all")
public final class ClzUtil {

    /**
     * 缓存已实例化过的loader
     */
    private final static Map<String, URLClassLoader> CACHE_LOADER = new ConcurrentHashMap<>();


    /**
     * 会在当前file所在的目录下生成class文件
     *
     * @param clzName 类名
     * @param file    java文件
     * @return 类加载之后反射生成的对象
     */
    public static Object compilerAndInstance(String clzName, File file) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int compilationResult = compiler.run(null, null, null, file.getAbsolutePath());
        if (compilationResult == 0) {
            try {
                //2.获取URL对象
                URL url = file.toURI().toURL();

                //3.创建URL类加载器
                URLClassLoader urlClassLoader;
                if (null == (urlClassLoader = CACHE_LOADER.get(url.toString()))) {
                    urlClassLoader = new URLClassLoader(new URL[]{url});
                }

                CACHE_LOADER.put(url.toString(), urlClassLoader);

                //4.通过urlClassLoader加载器调用loadClass方法传入类名动态加载class文件并获取class对象:会初始化静态块
                Class<?> targetClz = urlClassLoader.loadClass(clzName);

                //5.通过class对象创建实例
                assert targetClz != null;
                return targetClz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("load {} error", clzName, e);
            }
        } else {
            log.error("compiler error");
        }
        return null;
    }

    /**
     * 卸载类，如果当前load的classloader仅仅加载了这一个类，则顺便卸载loader
     *
     * @param clzFullName 类全限定名
     * @return 卸载结果
     */
    public static boolean uninstallClass(String clzFullName) {
        for (URLClassLoader loader : CACHE_LOADER.values()) {
            try {
                // private final Vector<Class<?>> classes = new Vector<>();
                Field field = ClassLoader.class.getDeclaredField("classes");

                field.setAccessible(true);
                Vector<Class<?>> loadedClz = (Vector<Class<?>>) field.get(loader);
                for (Class<?> clz : loadedClz) {
                    if (clz.getName().equals(clzFullName)) {
                        clz = null;
                        if (loadedClz.size() == 1) {
                            log.info("uninstall loader --> {}", loader);
                            loader = null;
                        }
                        return true;
                    }
                }

            } catch (Exception e) {
                log.error("uninstall fail", e);
            }
        }
        return false;
    }

}
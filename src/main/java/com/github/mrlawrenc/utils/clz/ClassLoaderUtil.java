package com.github.mrlawrenc.utils.clz;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author : MrLawrenc
 * date  2020/6/7 10:40
 * <p>
 * 两种方式打破双亲委派{@link ClassLoader#loadClass(String)}
 * <pre>
 *     1. 手动设置parent属性为null
 *     2. 改变loadclass内容，使 findLoadedClass(name);方法能获取到已经加载的类，详见ClassLoader line=570
 * </pre>
 */
@Slf4j
public class ClassLoaderUtil extends ClassLoader {
    public static final String PKG_SEPARATOR = ".";

    public static final String CLASS_END = ".class";


    public static final String JAR_END = ".jar";


    private final Map<String, Class<?>> loaded = new HashMap<>();


    /**
     * 推荐复写{@link ClassLoader#findClass(String)}方法，当加载不到的时候会调用自身的findClass
     */
    public Class<?> loadClass0(String classFullName, byte[] b) {
        Class<?> result = defineClass(classFullName, b, 0, b.length);
        loaded.put(classFullName, result);
        return result;
    }

    private void loadClass(File file) {
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (null != listFiles) {
                for (File currentFile : listFiles) {
                    loadClass(currentFile);
                }
            }
        } else {
            String fileName = file.getName();

            if (!fileName.contains(PKG_SEPARATOR)) {
                log.info("ignore file  : {}", fileName);
                return;
            }

            String endName = fileName.substring(fileName.lastIndexOf(PKG_SEPARATOR));

            if (CLASS_END.equals(endName)) {
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    byte[] bytes = new byte[inputStream.available()];
                    int read = inputStream.read(bytes);
                    if (read == -1) {
                        log.error("read class not finished");
                    }

                    String temp = file.getAbsolutePath()
                            .replaceAll(Matcher.quoteReplacement(File.separator), PKG_SEPARATOR);
                    if (temp.startsWith(PKG_SEPARATOR)) {
                        temp = temp.substring(1);
                    }
                    String className = temp.substring(0, temp.lastIndexOf(PKG_SEPARATOR));
                    log.info("load class : {}", className);
                    loadClass0(className, bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (JAR_END.equals(endName)) {
                log.info("load jar file");
            } else {
                log.info("ignore file  : {}", fileName);
            }
        }
    }


    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = findLoadedClass(name);
        if (result == null) {
            if (loaded.containsKey(name)) {
                result = loaded.get(name);
            } else {
                //交由App加载
                result = getSystemClassLoader().loadClass(name);
            }
        }
        return result;
    }

}
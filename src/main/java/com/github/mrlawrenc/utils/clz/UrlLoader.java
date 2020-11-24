package com.github.mrlawrenc.utils.clz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

/**
 * 该类加载器是非常规的双亲委派，而是优先尝试自身加载，如果加载不到才会委派到双亲加载。该类加载器也覆盖了大部分需要自定义加载器的场景
 * <p>
 * <pre>
 *      1.如果在创建该类加载器之前就已经确定需要加载哪些classpath的资源，则可以在构建器中指定url
 *      2.如果不确定需要加载的url资源，该资源是动态的，则可以使用{@link UrlLoader#addURL(URL)}方法来新增url
 *      3.如果需要打破双亲，建议使用{@link UrlLoader#findAndLoad(String)}方法来查找、加载class。
 *      4.如果需要遵循双亲委派，建议直接使用父类的{@link UrlLoader#loadClass(String)}方法来加载class
 *      5.如果需要加载指定文件夹下的jar文件，则可以使用{@link UrlLoader#loadJar(File)}方法
 *      6.如果需要加载指定的文件夹下的class文件，则可以使用{@link UrlLoader#loadClassFile(File, String)}方法
 *      7.如果需要编译java源码，且需要load，则使用{@link UrlLoader#loadJavaSource(String, String)}
 * </pre>
 *
 * @author hz20035009-逍遥
 * date   2020/11/23 14:21
 */
@Slf4j
@SuppressWarnings("all")
public class UrlLoader extends URLClassLoader {
    //打为jar之后为null 建议使用   String path = System.getProperty("java.class.path");获取当前classpath
    //private static String classPath = new File(UrlLoader.class.getResource("/").getFile()).getAbsolutePath();


    private final URL[] urls;

    /**
     * @param urls url若是文件类型，则应该是class根路径 E:\openSource\gtdq_study\target\classes，可以由new File().toURI().toURL()生成url
     *             若是jar，则应该为jar的路径 如 D:\B\migration-common-0.0.1-SNAPSHOT.jar
     *             如果传入urls为null，则会默认当前项目的classpath
     * @throws MalformedURLException e
     */
    public UrlLoader(URL[] urls) throws MalformedURLException {
        super(urls == null ? new URL[]{UrlLoader.projectClassPathUrl()} : urls);
        if (urls == null) {
            urls = new URL[]{UrlLoader.projectClassPathUrl()};
        }
        this.urls = urls;
    }


    public void loadAll() throws URISyntaxException, IOException, ClassNotFoundException {
        for (URL url : urls) {
            File parentFile = new File(url.toURI());
            load(parentFile, parentFile.getAbsolutePath());
        }
    }


    /**
     * 加载所有 {@link UrlLoader#urls}下的class和jar,内部使用
     *
     * @param file      文件
     * @param classPath classpath
     * @throws ClassNotFoundException e
     */
    private void load(File file, String classPath) throws ClassNotFoundException, IOException {
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (null != listFiles) {
                for (File currentFile : listFiles) {
                    load(currentFile, classPath);
                }
            }
        } else {
            String fileName = file.getName();
            String endName = fileName.substring(fileName.lastIndexOf("."));
            if (".class".equals(endName)) {
                String temp = file.getAbsolutePath().replaceAll(Matcher.quoteReplacement(classPath), "");
                String className = temp.replaceAll(Matcher.quoteReplacement(File.separator), ".").substring(1).replace(".class", "");
                Class<?> aClass = findAndLoad(className);
                System.out.println(aClass + "-->" + aClass.getClassLoader());
            } else if (".jar".equals(endName)) {
                //jar文件
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> jarEntry = jarFile.entries();
                while (jarEntry.hasMoreElements()) {
                    JarEntry entry = jarEntry.nextElement();
                    //name 格式 org/springframework/boot/loader/ClassPathIndexFile.class
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        String className = name.replace(".class", "").replace("/", ".");
                        findAndLoad(className);
                    }
                }
            }
        }
    }

    /**
     * 根据class全限定名查找class，如果当前类加载器已经加载，则返回，否则尝试加载，若失败，则委托给双亲
     *
     * @param className 名
     * @return class对象
     * @throws ClassNotFoundException 无法找到，且无法加载
     */
    public Class<?> findAndLoad(String className) throws ClassNotFoundException {
        //寻找是否已经在加载关联的类的时候已经被加载了
        Class<?> loadedClass = findLoadedClass(className);
        if (loadedClass != null) {
            return loadedClass;
        }
        try {
            //使用当前类加载器加载 会到url的路径去扫描所有的文件，匹配当前className，并读取byte[]数据，加载class
            return findClass(className);
        } catch (Throwable e) {
            //委托双亲加载
            return loadClass(className);
        }
    }

    /**
     * 加载file下面的所有 .jar文件
     */
    public void loadJar(File file) throws IOException {
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (null != listFiles) {
                for (File currentFile : listFiles) {
                    loadJar(currentFile);
                }
            }
        } else {
            String fileName = file.getName();
            String endName = fileName.substring(fileName.lastIndexOf("."));

            if (!".jar".equals(endName)) {
                return;
            }
            //偷个懒，直接新加url
            addURL(file.toURI().toURL());
           /* //jar文件
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> jarEntry = jarFile.entries();
            while (jarEntry.hasMoreElements()) {
                JarEntry entry = jarEntry.nextElement();
                //name 格式 org/springframework/boot/loader/ClassPathIndexFile.class
                String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }

                String className = name.replace(".class", "").replace("/", ".");
                if (this.findLoadedClass(className) != null) continue;
                try (InputStream inputStream = jarFile.getInputStream(entry)) {
                    byte[] data = new byte[inputStream.available()];
                    inputStream.read(data);
                    //load
                    defineClass(className, data, 0, data.length);
                }

            }*/
        }
    }

    /**
     * 加载java资源，并load
     *
     * @param name       类名，非全限定名
     * @param javaSource java源码
     * @return load之后的class
     * @throws Exception e
     */
    public Class<?> loadJavaSource(String name, String javaSource) throws Exception {
        Map<String, byte[]> map = CompileUtil.compileSourceCode2Memory(name, javaSource);
        if (map.size() == 1) {
            Iterator<Map.Entry<String, byte[]>> iterator = map.entrySet().iterator();
            Map.Entry<String, byte[]> entry = iterator.next();
            String className = entry.getKey();
            byte[] data = entry.getValue();
            return defineClass(className, data, 0, data.length);
        }
        throw new ClassNotFoundException("compile " + name + " fail");
    }

    /**
     * 加载该file下的所有以.class结尾的文件
     *
     * @param file      顶层目录
     * @param classPath classpath  如 E:\openSource\gtdq_study\target\classes
     * @throws IOException load异常
     */
    public void loadClassFile(File file, String classPath) throws IOException {
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (null != listFiles) {
                for (File currentFile : listFiles) {
                    loadClassFile(currentFile, classPath);
                }
            }
        } else {
            String fileName = file.getName();
            String endName = fileName.substring(fileName.lastIndexOf("."));
            if (!".class".equals(endName)) {
                return;
            }

            try (FileInputStream inputStream = new FileInputStream(file)) {
                String temp = file.getAbsolutePath().replaceAll(Matcher.quoteReplacement(classPath), "");
                String className = temp.replaceAll(Matcher.quoteReplacement(File.separator), ".").substring(1).replace(".class", "");

                if (this.findLoadedClass(className) != null) return;

                if (StringUtils.isEmpty(className)) {
                    return;
                }
                byte[] bytes = new byte[(int) file.length()];
                inputStream.read(bytes, 0, bytes.length);
                Class<?> aClass = defineClass(className, bytes, 0, bytes.length);
                System.out.println(aClass + "-->" + aClass.getClassLoader());
            }
        }
    }


    /**
     * 返回类全限定名
     *
     * @param classFile class文件
     * @param classPath classpath目录，如 E:\openSource\gtdq_study\target\classes
     * @return class 全限定名 com.a.App
     */
    public static String getClassName(File classFile, String classPath) {
        String temp = classFile.getAbsolutePath().replaceAll(Matcher.quoteReplacement(classPath), "");
        return temp.replaceAll(Matcher.quoteReplacement(File.separator), ".").substring(1).replace(".class", "");
    }

    /**
     * @return 获取当前工程的url，如果是jar包，则类似D:\B\migration-common-0.0.1-SNAPSHOT.jar结构，如果不是则类似E:\openSource\gtdq_study\target\classes结构
     * @throws MalformedURLException 转为url失败
     */
    public static URL projectClassPathUrl() throws MalformedURLException {
        String projectPath = System.getProperty("user.dir");
        System.out.println("projectPath:" + projectPath);

        //兼容打包为jar之后获取classpath
        URL classPathUrl = null;
        String path = System.getProperty("java.class.path");
        System.out.println("path:" + path);
        for (String p : path.split(";")) {
            //打为jar之后是相对路径，转为绝对路径
            p = new File(p).getAbsolutePath();
            if (p.contains(projectPath)) {
                classPathUrl = new File(p).toURI().toURL();
            }
        }
        return classPathUrl;
    }
}
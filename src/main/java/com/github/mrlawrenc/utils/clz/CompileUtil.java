package com.github.mrlawrenc.utils.clz;

import lombok.extern.slf4j.Slf4j;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.Map;


/**
 * @author : MrLawrenc
 * date  2020/9/26 20:09
 * <p>
 * 字节码编译工具
 */
@Slf4j
public class CompileUtil {

    /**
     * 这是一个标准编译方法.编译java文件,class输出到当前源文件所处目录
     *
     * @param file java源文件
     */
    public void compileFile(File file) throws Exception {
        compileFileErrorOut2Writer(file, null);
    }

    /**
     * 这是一个标准编译方法.编译java文件到writer
     *
     * @param writer 编译错误输出位置，为null，则使用System.err输出
     * @param file   要编译的源文件 java源文件
     * @see CompileUtil#compileFileOutMemory(Map, File)
     * @see CompileUtil#compileSourceCode2Memory(String, String, Map)
     */
    public void compileFileErrorOut2Writer(File file, Writer writer) throws Exception {
        //获取系统Java编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        //获取Java文件管理器
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        //通过源文件获取到要编译的Java类源码迭代器，包括所有内部类，其中每个类都是一个 JavaFileObject，也被称为一个汇编单元
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(file);
        //生成编译任务 第一个参数是输出到哪个位置，为null会默认输出到java文件同级
        JavaCompiler.CompilationTask task = compiler.getTask(writer, fileManager, null, null, null, compilationUnits);

        //执行编译任务
        task.call();

        fileManager.close();
    }


    /**
     * 转化一个标准的JavaFileManager为可以将字节码输出到内存的JavaFileManager
     *
     * @param bytes 字节码存储对象 key为全类名 value为字节码数组
     * @return ForwardingJavaFileManager对象
     */
    public ForwardingJavaFileManager<StandardJavaFileManager> out2MemoryJavaFileManager(StandardJavaFileManager fileManager, Map<String, byte[]> bytes) {
        return new ForwardingJavaFileManager<StandardJavaFileManager>(fileManager) {
            @Override
            public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
                                                       String className,
                                                       JavaFileObject.Kind kind,
                                                       FileObject sibling) throws IOException {
                if (kind == JavaFileObject.Kind.CLASS) {
                    return new SimpleJavaFileObject(URI.create(className + ".class"), JavaFileObject.Kind.CLASS) {
                        @Override
                        public OutputStream openOutputStream() {
                            return new FilterOutputStream(new ByteArrayOutputStream()) {
                                @Override
                                public void close() throws IOException {
                                    out.close();
                                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                                    log.info("cache class data --> {}", className);
                                    bytes.put(className, bos.toByteArray());
                                }
                            };
                        }
                    };
                } else {
                    return super.getJavaFileForOutput(location, className, kind, sibling);
                }
            }
        };
    }

    /**
     * 编译java文件，且字节码输出在内存中
     *
     * @param bytes    字节码存储对象 key为全限定类名 value为字节码数组
     * @param javaFile java源文件
     * @throws Exception 编译异常
     */
    public void compileFileOutMemory(Map<String, byte[]> bytes, File javaFile) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(javaFile);

        ForwardingJavaFileManager<StandardJavaFileManager> jfm = out2MemoryJavaFileManager(fileManager, bytes);

        JavaCompiler.CompilationTask task = compiler.getTask(null, jfm, null, null, null, compilationUnits);

        task.call();
        jfm.close();
    }

    /**
     * 将java源代码编译到内存中
     * 测试case如下:
     * <pre>
     *          String sourceCode = "package com;public class Test{\n" +
     *                 "  @Override\n" +
     *                 "  public String toString() {\n" +
     *                 "    return \"hello java compiler\";\n" +
     *                 "  }\n" +
     *                 "}";
     *
     *         Map<String, byte[]> map = new HashMap<>();
     *         compileUtil.compileSourceCode2Memory("Test", sourceCode, map);
     *         map.keySet().forEach(System.out::println);
     * </pre>
     *
     * @param className  类名
     * @param bytes      编辑之后的class数据存储在该map中，key为全限定名，value为class字节数组
     * @param sourceCode java源代码
     * @throws Exception 编译出错
     */
    public void compileSourceCode2Memory(String className, String sourceCode, Map<String, byte[]> bytes) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        ForwardingJavaFileManager<StandardJavaFileManager> jfm = out2MemoryJavaFileManager(fileManager, bytes);

        //定义要编译的源文件
        SimpleJavaFileObject sourceJavaFileObject = new SimpleJavaFileObject(URI.create(className + ".java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharBuffer getCharContent(boolean b) {
                return CharBuffer.wrap(sourceCode);
            }
        };
        Iterable<? extends JavaFileObject> iterable = new Iterable<JavaFileObject>() {
            int i = 0;

            @Override
            public Iterator<JavaFileObject> iterator() {
                return new Iterator<JavaFileObject>() {
                    @Override
                    public boolean hasNext() {
                        if (i == 0) {
                            i++;
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public JavaFileObject next() {
                        return sourceJavaFileObject;
                    }
                };
            }
        };
        JavaCompiler.CompilationTask task = compiler.getTask(null, jfm, null, null, null, iterable);
        Boolean success = task.call();
        log.info("compile {}", success ? "success" : "fail");
        jfm.close();
    }
}
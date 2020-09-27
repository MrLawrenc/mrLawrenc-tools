package com.github.mrlawrenc.cache.serializable.impl;


import com.github.mrlawrenc.cache.serializable.SerializableCache;
import com.github.mrlawrenc.utils.serializable.SerializableUtil;
import com.github.mrlawrenc.thread.SerializableRunnable;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hz20035009-逍遥
 * date  2020/5/26 9:43
 * 文件缓存
 */
@Slf4j
@Accessors(chain = true)
public class FileSerializableCache implements SerializableCache {

    private static final AtomicInteger TASK_NO = new AtomicInteger(0);

    /**
     * 文件名前置
     */
    @Setter
    private String fileName = "test.task";


    @Override
    public boolean save(String absolutePath, SerializableRunnable runnable) {
        try (FileOutputStream outputStream = new FileOutputStream(new File(absolutePath + "/" + fileName + TASK_NO.getAndIncrement()))) {
            outputStream.write(SerializableUtil.beanToXml(runnable).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public CompletableFuture<?> asyncSave(String absolutePath, SerializableRunnable runnable) {

        return CompletableFuture.supplyAsync(() -> save(absolutePath, runnable));
    }

    @Override
    public List<SerializableRunnable> list(String absolutePath) {
        return list(absolutePath, false);
    }

    @Override
    public List<SerializableRunnable> asyncList(String absolutePath) {
        return list(absolutePath, true);
    }

    private List<SerializableRunnable> list(String absolutePath, boolean async) {
        ArrayList<SerializableRunnable> result = new ArrayList<>();
        File file = new File(absolutePath);
        if (file.isDirectory()) {
            String[] list = file.list((dir, name) -> name.contains(fileName));
            if (list == null || list.length == 0) {
                result.add(() -> log.info("Not deserialize task"));
                return result;
            }
            if (log.isDebugEnabled()) {
                log.debug("Deserialize task  num:{}", list.length);
            }

            if (async) {
                CompletableFuture<?>[] futures = Arrays.stream(list).map(currentFile ->
                        CompletableFuture.runAsync(() -> doList(currentFile, result))).toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(futures).join();
            } else {
                Arrays.stream(list).forEach(currentFile -> doList(currentFile, result));
            }

        } else {
            result.add(() -> log.info("No deserialize task!"));
        }
        return result;
    }

    private void doList(String currentFile, List<SerializableRunnable> result) {
        File file = new File(currentFile);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);

            String xml = new String(bytes);

            SerializableRunnable runnable = SerializableUtil.xmlToBean(xml);
            result.add(runnable);
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.delete();
    }

}
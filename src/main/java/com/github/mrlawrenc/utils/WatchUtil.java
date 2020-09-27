package com.github.mrlawrenc.utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author MrLawrenc
 * date  2020/5/28
 * @see StandardWatchEventKinds
 */
public class WatchUtil {
    private BiConsumer<String, WatchEvent<?>> allListener = null;
    private BiConsumer<String, WatchEvent<?>> createListener = null;
    private BiConsumer<String, WatchEvent<?>> deleteListener = null;
    private BiConsumer<String, WatchEvent<?>> modifyListener = null;
    private BiConsumer<String, WatchEvent<?>> overflowListener = null;

    private Thread listenerThread = null;


    /**
     * 给目录添加一个监听钩子
     *
     * @param dir 监听的文件目录
     * @throws IOException 异常
     */
    public void addFileWatch(String dir) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path p = Paths.get(dir);
        p.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_CREATE);
        listenerThread = new Thread(() -> {
            try {
                while (true) {
                    WatchKey watchKey = watchService.take();
                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    for (WatchEvent<?> event : watchEvents) {
                        if (allListener != null) {
                            allListener.accept(dir, event);
                        }
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            if (overflowListener != null) {
                                overflowListener.accept(dir, event);
                            }
                        } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            if (createListener != null) {
                                createListener.accept(dir, event);
                            }
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            if (deleteListener != null) {
                                deleteListener.accept(dir, event);
                            }
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            if (modifyListener != null) {
                                modifyListener.accept(dir, event);
                            }
                        }
                    }
                    watchKey.reset();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        listenerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                watchService.close();
            } catch (Exception ignored) {
            }
        }));
    }


    public void addAllListener(BiConsumer<String, WatchEvent<?>> consumer) {
        this.allListener = consumer;
    }

    public void addCreateListener(BiConsumer<String, WatchEvent<?>> consumer) {
        this.createListener = consumer;
    }

    public void addDeleteListener(BiConsumer<String, WatchEvent<?>> consumer) {
        this.deleteListener = consumer;
    }

    public void addModifyListener(BiConsumer<String, WatchEvent<?>> consumer) {
        this.modifyListener = consumer;
    }

    public void addOverflowListener(BiConsumer<String, WatchEvent<?>> consumer) {
        this.overflowListener = consumer;
    }


    public void close() {
        Runtime.getRuntime().removeShutdownHook(listenerThread);
    }
}

package com.github.mrlawrenc.utils.jctool;

/**
 * @author hz20035009-逍遥
 * date   2020/9/28 14:30
 * <p>
 * 非阻塞 Map
 * ConcurrentAutoTable（后面几个map/set结构的基础）
 * NonBlockingHashMap
 * NonBlockingHashMapLong
 * NonBlockingHashSet
 * NonBlockingIdentityHashMap
 * NonBlockingSetInt
 * NonBlockingHashMap 是对 ConcurrentHashMap 的增强，对多 CPU 的支持以及高并发更新提供更好的性能。
 * <p>
 * NonBlockingHashMapLong 是 key 为 Long 型的 NonBlockingHashMap。
 * <p>
 * NonBlockingHashSet 是对 NonBlockingHashMap 的简单包装以支持 set 的接口。
 * <p>
 * NonBlockingIdentityHashMap 是从 NonBlockingHashMap 改造来的，使用 System.identityHashCode() 来计算哈希。
 * <p>
 * NonBlockingSetInt 是一个使用 CAS 的简单的 bit-vector。
 * <p>
 * 非阻塞 Queue
 * JCTools 提供的非阻塞队列分为 4 类，可以根据不同的应用场景选择使用：
 * <p>
 * SPSC-单一生产者单一消费者（有界和无界）
 * MPSC-多生产者单一消费者（有界和无界）
 * SPMC-单生产者多消费者（有界）
 * MPMC-多生产者多消费者（有界）
 * “生产者”和“消费者”是指“生产线程”和“消费线程”。
 */
public class JcTool {
}
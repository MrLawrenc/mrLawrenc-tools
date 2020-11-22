package com.github.mrlawrenc.utils.collections;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * 跳表，类似b+树，多叉树,大数据查找时可用来替换二叉树
 *
 * @author : LiuMingyao
 * 2020/3/9 10:08
 */
@SuppressWarnings("all")
public class SkipTable {
    /**
     * 非公平的读写锁
     */
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();

    private static final SecureRandom RANDOM = new SecureRandom();
    /**
     * 节点类型
     * <pre>
     *     1.0-->数据节点
     *     2.-1-->头节点
     *     3.1-->尾节点
     * </pre>
     */
    private static final byte DATA_NODE = 0;
    private static final byte HEAD_NODE = -1;
    private static final byte TAIL_NODE = 1;

    /**
     * key->层数  value->该层存储数据的数量。
     * 从第1层开始计数。
     */
    private Map<Integer, Integer> map = new HashMap<>();

    /**
     * 默认升层概率阈值，当小于该值时会升层，值越小，层数低的概率越高
     */
    private static final double DEFAULT_PROBABILITY = 0.6;

    /**
     * 决策函数，插入数据时决定是否需要升层。默认随机决策，阈值参考{@link SkipTable#DEFAULT_PROBABILITY}
     */
    private Supplier<Boolean> decision;

    /**
     * 头和尾节点
     */
    private Node head;
    private Node tail;

    /**
     * 跳表实际层数
     */
    private int tierSum;

    /**
     * 是否开启debug，若开启每次新加入一个{@link Node}都会输出跳跃表结构
     *
     * @see {@link #dump()}
     */
    private boolean debug;


    public SkipTable() {
        this(false, () -> RANDOM.nextDouble() < DEFAULT_PROBABILITY);
    }

    public SkipTable(boolean debug) {
        this(debug, () -> RANDOM.nextDouble() < DEFAULT_PROBABILITY);
    }

    public SkipTable(double probability) {
        this(false, () -> RANDOM.nextDouble() < probability);
    }

    public SkipTable(boolean debug, double probability) {
        this(debug, () -> RANDOM.nextDouble() < probability);
    }

    public SkipTable(Supplier<Boolean> decision) {
        this(false, decision);
    }

    public SkipTable(Boolean debug, Supplier<Boolean> decision) {
        this.decision = decision;
        initNode(debug);
    }


    private void initNode(boolean debug) {
        if (debug) {
            System.out.println("skipTable debug enable");
        }
        this.debug = debug;
        this.head = new Node(null, HEAD_NODE);
        this.tail = new Node(null, TAIL_NODE);
        head.right = tail;
        tail.left = head;
        this.tierSum = 1;
    }

    /**
     * @param value 待查找的值
     * @return 离待查找值最近的左边节点, 待查找值范围满足：[左边节点值，右边节点值),可能为head节点
     */
    public Node findNearLeft(int value) {
        Node current = head;
        readLock.lock();
        try {
            while (true) {
                while (current.right.type != TAIL_NODE && current.right.value < value) {
                    current = current.right;
                }
                if (current.down == null) {
                    //最后一层
                    return current;
                } else {
                    current = current.down;
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 当前层的数据量加1
     *
     * @param currentTier 层数
     */
    private void incrDataNum(int currentTier) {
        Integer num = map.get(currentTier);
        map.put(currentTier, num == null ? 1 : ++num);
    }


    public synchronized void insert(int value) {
        writeLock.lock();
        Node nearLeft = findNearLeft(value);
        Node newNode = new Node(value);
        newNode.right = nearLeft.right;
        newNode.left = nearLeft;

        nearLeft.right.left = newNode;
        nearLeft.right = newNode;
        incrDataNum(1);
        int currentTier = 1;
        while (decision.get()) {
            if (currentTier >= tierSum) {
                tierSum++;
                Node upHead = new Node(null, HEAD_NODE);
                Node upTail = new Node(null, TAIL_NODE);

                head.up = upHead;
                upHead.down = head;
                upHead.right = upTail;

                tail.up = upTail;
                upTail.down = tail;
                upTail.left = upHead;

                head = upHead;
                tail = upTail;
            }
            while (nearLeft.up == null) {
                nearLeft = nearLeft.left;
            }
            nearLeft = nearLeft.up;
            currentTier++;
            //升层
            Node newTierNode = new Node(value);


            newTierNode.left = nearLeft;
            newTierNode.right = nearLeft.right;
            newTierNode.down = newNode;

            nearLeft.right.left = newTierNode;
            nearLeft.right = newTierNode;
            newNode.up = newTierNode;


            newNode = newTierNode;
            incrDataNum(currentTier);
        }
        writeLock.unlock();
        if (debug) {
            System.out.println("insert[" + value + "],The skipTable structure is as follows");
            dump();
        }
    }

    public void dump() {
        readLock.lock();
        Node tempHead = head;
        try {
            while (true) {
                Node temp = tempHead.right;
                System.out.print("head");
                while (temp.type == DATA_NODE) {
                    System.out.print(" -> " + temp.value);
                    temp = temp.right;
                }
                tempHead = tempHead.down;
                System.out.print(" -> tail\n");
                if (tempHead == null) {
                    return;
                }
            }
        } finally {
            readLock.unlock();
        }
    }


    public boolean contains(Integer value) {
        if (value == null) throw new NullPointerException();
        return value.equals(findNearLeft(value).value);
    }

    /**
     * 第一层的数据即为数据总量
     *
     * @return 存储数据量
     */
    public int size() {
        return map.get(1) == null ? 0 : map.get(1);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private class Node {
        // data value
        public Integer value;
        //node type
        private byte type;

        // links
        public Node up, down, left, right;


        /**
         * 默认数据节点
         */
        public Node(Integer value) {
            this(value, DATA_NODE);
        }


        public Node(Integer value, byte type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "value=" + value +
                    ", type=" + type +
                    ",left=" + (left != null ? left.value : "null") +
                    ",right=" + (right != null ? right.value : "null") +
                    '}';
        }
    }

}
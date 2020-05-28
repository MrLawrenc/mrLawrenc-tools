package com.github.mrLawrenc.thread;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * @author hz20035009-逍遥
 * date  2020/5/22 10:06
 */
public interface SerializableCallable<V> extends Serializable, Callable<V> {


}
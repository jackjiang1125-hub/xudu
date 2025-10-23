// util/InflightLocks.java
package com.xudu.center.zlm.util;

import java.util.concurrent.*;

public final class InflightLocks {
  private static final ConcurrentHashMap<String, Semaphore> MAP = new ConcurrentHashMap<>();
  private InflightLocks(){}
  public static void acquire(String key) {
    Semaphore s = MAP.computeIfAbsent(key, k -> new Semaphore(1));
    try { s.acquire(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
  }
  public static void release(String key) {
    Semaphore s = MAP.get(key);
    if (s != null) s.release();
  }
}

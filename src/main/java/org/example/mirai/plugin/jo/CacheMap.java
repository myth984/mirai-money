package org.example.mirai.plugin.jo;


import java.util.*;

public class CacheMap<K, V> extends HashMap<K, V> {

    private static CacheMap<Object, Object> defaultInstance;

    private final HashMap<K, TimeOut> timeMap = new HashMap<>();

    public static synchronized final CacheMap<Object, Object> getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new CacheMap<Object, Object>();
        }
        return defaultInstance;
    }

    static class TimeOut {
        Long createTime;
        Long timeOut;

        TimeOut(Long timeOut) {
            this.timeOut = timeOut;
            this.createTime = System.currentTimeMillis();
        }

        public Long getCreateTime() {
            return createTime;
        }

        public Long getTimeOut() {
            return timeOut;
        }

        public void setTimeOut(Long timeOut) {
            this.timeOut = timeOut;
        }

        public boolean isOverdue() {
            return System.currentTimeMillis() - this.timeOut >= this.createTime;
        }
    }


    private class ClearThread extends Thread {
        ClearThread() {
            setName("clear cache thread");
        }

        public void run() {
            while (true) {
                try {
                    for (Object key : timeMap.keySet().toArray()) {
                        TimeOut timeOut = timeMap.get(key);
                        if (timeOut.isOverdue()) {
                            synchronized (defaultInstance) {
                                timeMap.remove(key);
                                defaultInstance.remove(key);
                            }
                        }
                    }
                    // 每五秒检测一次
                    Thread.sleep(5000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public V put(K key, V value) {
        TimeOut timeOut = new TimeOut(1000L);
        return put(key, value, timeOut);
    }

    @Override
    public V get(Object key) {
        TimeOut timeOut = this.timeMap.get(key);
        if (timeOut.isOverdue()) {
            // 如果过期了返回空
            // 并且移除这个对象
            synchronized (defaultInstance) {
                timeMap.remove(key);
                this.remove(key);
            }
            return null;
        } else {
            return super.get(key);
        }

    }

    public V put(K key, V value, TimeOut time) {
        timeMap.put(key, time);
        return super.put(key, value);
    }


    public CacheMap() {
        new ClearThread().start();
    }

}
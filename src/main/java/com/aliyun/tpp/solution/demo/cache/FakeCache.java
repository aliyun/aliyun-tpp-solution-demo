package com.aliyun.tpp.solution.demo.cache;

import com.aliyun.tpp.service.cache.Cache;

/**
 * author: oe
 * date:   2022/3/17
 * comment:无实际作用的缓存实现, 仅用于测试
 */
public class FakeCache implements Cache<String> {

    @Override
    public boolean put(String key, String value, long timeoutMills) {
        return true;
    }

    @Override
    public String get(String key, String defaultValue) {
        return defaultValue;
    }

    @Override
    public void init() {

    }

    @Override
    public void clean() {

    }

    @Override
    public void close() {

    }
}


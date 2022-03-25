package com.aliyun.tpp.solution.demo.rerank.filter;

import com.alibaba.fastjson.JSON;
import com.aliyun.tpp.service.cache.Cache;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * author: oe
 * date:   2021/8/31
 * comment:曝光过滤，最终结果需要过滤掉曝光缓存的数据
 * 注意：这里只是个例子，不对您的业务效果负责，请根据自己的需求改造
 */
public class ExposureFilter {

    @Getter
    private Cache<String> cache;

    public ExposureFilter(Cache<String> cache) {
        this.cache = cache;
    }

    @PostConstruct
    public void init() {
        cache.init();
    }

    @PreDestroy
    public void close() {
        cache.close();
    }

    //构造key bizId-业务id
    public static String buildKey(String bizId) {
        return Cache.buildKey(":", "ExposureCache", bizId);
    }

    // 获取bizId下所有曝光过的数据，这些都需要过滤
    //"[\"b1ab4c4eece434595c13a6fe159adddf\"]"
    public List<String> get(String bizId, List<String> defaultValue) {
        String value = cache.get(buildKey(bizId), null);
        if (value != null) {
            return JSON.parseArray(value, String.class);
        } else {
            return defaultValue;
        }
    }

    //写入bizId下所有曝光数据，以供下次过滤
    public boolean put(String bizId, List<String> value, long timeoutMills) {
        return cache.put(buildKey(bizId), JSON.toJSONString(value), timeoutMills);
    }
}

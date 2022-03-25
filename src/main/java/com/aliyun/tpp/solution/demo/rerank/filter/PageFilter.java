package com.aliyun.tpp.solution.demo.rerank.filter;

import com.alibaba.fastjson.JSON;
import com.aliyun.tpp.service.cache.Cache;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.List;

/**
 * author: oe
 * date:   2021/8/31
 * comment:分页过滤，最终结果需要过滤掉分页缓存的数据
 * 注意：这里只是个例子，不对您的业务效果负责，请根据自己的需求改造
 */
public class PageFilter {

    /**
     * 校验分页是否完整
     * 例如：当前请求第5页，需要拿到前4页的缓存，如果有一页缺失，则缓存不完整
     */
    @Getter
    private boolean isCacheCompletable = true;

    /**
     * 分页缓存缺页数量
     */
    @Getter
    private int missingPageNums = 0;


    @Getter
    private Cache<String> cache;

    public PageFilter(Cache<String> cache) {
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

    //构造key bizId-业务id pageIndex-第几页
    public static String buildKey(String bizId, int pageIndex) {
        return Cache.buildKey(":", "PageCache", bizId, String.valueOf(pageIndex));
    }

    // 获取前面几页所有的缓存，这些都需要过滤
    //bizId-业务id pageIndex-第几页
    //"[\"b1ab4c4eece434595c13a6fe159adddf\"]"
    public List<String> get(String bizId, int pageIndex, List<String> defaultValue) {
        List<String> list = new LinkedList<>();
        for (int i = 0; i < pageIndex; i++) {
            String key = buildKey(bizId, i);
            String value = cache.get(key, null);
            if (value == null || value.isEmpty()) {
                this.missingPageNums++;
            } else {
                list.addAll(JSON.parseArray(value, String.class));
            }
        }
        if (this.missingPageNums > 0) {
            this.isCacheCompletable = false;
        } else {
            this.isCacheCompletable = true;
        }
        if (list.isEmpty()) {
            list.addAll(defaultValue);
        }
        return list;
    }

    //写入本次调用页的缓存
    //bizId-业务id pageIndex-第几页
    public boolean put(String bizId, int pageIndex, List<String> value, long timeoutMills) {
        String key = buildKey(bizId, pageIndex);
        return cache.put(key, JSON.toJSONString(value), timeoutMills);
    }

    //如果还缺页，说明还需要过滤一些没查到的数据。
    // pageSize 每一页多少数量
    // startIndex 应该尽量取靠后的，因为靠前的分数高，曝光概率大，很有可能就是用户看过的
    public <T> List<T> missingFilter(List<T> list, int pageSize) {
        if (list != null && isCacheCompletable) {
            int total = list.size();
            int endIndex = total;
            int startIndex = Math.max(0, getMissingPageNums() * pageSize);
            //待过滤的数据，超过了现存的总数量，至少保留一页
            if (startIndex > total) {
                if (total <= pageSize) {
                    //现存不够一页,当前数据即为最后一页
                    return list;
                } else {
                    //现存超过一页，返回最后一页
                    startIndex = total - pageSize;
                    endIndex = pageSize;
                    return list.subList(startIndex, endIndex);
                }
            }
        }
        return list;
    }

}

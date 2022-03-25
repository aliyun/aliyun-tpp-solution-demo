package com.aliyun.tpp.solution.demo.match;

import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.service.rec.Match;
import com.aliyun.tpp.service.redis.RedisClient;
import com.aliyun.tpp.service.redis.RedisConfig;
import lombok.Getter;
import com.aliyun.tpp.solution.demo.data.RecommendContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/8/30
 * comment:redis召回
 */
public abstract class RedisMatch<V> implements Match<RecommendContext, V> {

    @Getter
    private RedisClient redisClient;

    public RedisMatch(RedisConfig redisConfig) {
        this.redisClient = ServiceProxyHolder.getService(redisConfig, ServiceLoaderProvider.getSuperLoaderEasy(RedisClient.class)); //获取client
    }

    @PostConstruct
    public void init() {
        redisClient.init();
    }

    @PreDestroy
    public void destroy() {
        if (redisClient != null) {
            redisClient.destroy();
        }
    }

    //召回
    public final List<V> match(RecommendContext context) throws Exception {
        checkParams(context);
        List<V> results = new LinkedList<>();
        List<String> keys = buildKey(context);
        for (String key : keys) {
            try {
                List<String> list = redisClient.lrange(key, 0, -1);
                if (list != null && list.size() > 0) {
                    List<String> value = list.stream().filter(line -> line != null && line.length() > 0).collect(Collectors.toList());
                    results.addAll(parseValue(context, value));
                } else {
                    //记数据日志，用于排查监控
                    context.getDataTraceLog().put(this.getClass().getName() + ".key=" + key, "value is empty");
                }
            } catch (Exception e) {
                //某几个key失败，记错误日志,
                context.getContextLogger().error("jedis.lrange error.key=" + key, e);
            }
        }
        return results;
    }

    protected void checkParams(RecommendContext context) {
        if (context.getUserId() == null || context.getUserId().isEmpty()) {
            throw new IllegalArgumentException("userId is empty");
        }
    }

    //构造key
    protected abstract List<String> buildKey(RecommendContext context) throws Exception;

    //解析value
    //空结果应该落日志排查,  不允许抛异常
    protected abstract List<V> parseValue(RecommendContext context, List<String> value);
}

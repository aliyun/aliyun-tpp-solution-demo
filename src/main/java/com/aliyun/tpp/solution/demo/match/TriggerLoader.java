package com.aliyun.tpp.solution.demo.match;

import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.service.redis.RedisClient;
import com.aliyun.tpp.service.redis.RedisConfig;
import com.aliyun.tpp.service.step.Step;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.match.data.Trigger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/9/3
 * comment:根据userId加载trigger Item
 */
public class TriggerLoader implements Step<RecommendContext, List<Trigger.Value>> {

    private RedisClient redisClient;

    public TriggerLoader(RedisConfig redisConfig) {
        this.redisClient = ServiceProxyHolder.getService(redisConfig, ServiceLoaderProvider.getSuperLoaderEasy(RedisClient.class));//获取client
    }

    @Override
    public void init() {
        redisClient.init();
    }

    @Override
    public boolean skip(RecommendContext context) {
        return !context.isI2iMatchSwitch() && (context.getUserId() == null || context.getUserId().isEmpty());
    }

    @Override
    public void destroy() {
        if (redisClient != null) {
            redisClient.destroy();
        }
    }

    @Override
    public List<Trigger.Value> invoke(RecommendContext context) throws Exception {
        String userId = context.getUserId();
        String key = new Trigger.Key(userId).buildKey(context.getBizId());
        List<String> value = redisClient.lrange(key, 0, -1)
                .stream().filter(line -> line != null && line.length() > 0)
                .collect(Collectors.toList());
        return parseValue(context, value);

    }

    private List<Trigger.Value> parseValue(RecommendContext context, List<String> value) {
        if (value == null || value.isEmpty()) {
            context.getEmptyTraceLog().put(this.getClass().getName(), "List<Trigger.Value> is empty");
            return Collections.emptyList();
        }
        List<Trigger.Value> list = new ArrayList<>(value.size());
        value.forEach(item -> {
            Trigger.Value triggerValue = Trigger.Value.parseValue(item);
            list.add(triggerValue);
        });
        return list;
    }
}

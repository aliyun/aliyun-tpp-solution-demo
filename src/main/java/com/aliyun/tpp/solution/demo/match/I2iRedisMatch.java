package com.aliyun.tpp.solution.demo.match;


import com.aliyun.tpp.service.redis.RedisConfig;
import lombok.Getter;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.match.data.I2i;
import com.aliyun.tpp.solution.demo.match.data.Trigger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/8/30
 * comment:基于redis的i2i召回
 */
public class I2iRedisMatch extends RedisMatch<I2i.Value>{

    @Getter
    private TriggerLoader triggerLoader;

    public I2iRedisMatch(RedisConfig redisConfig, TriggerLoader triggerLoader) {
        super(redisConfig);
        this.triggerLoader = triggerLoader;
    }

    @Override
    public void init() {
        super.init();
        triggerLoader.init();
    }

    @Override
    public void destroy() {
        if (triggerLoader != null) {
            triggerLoader.destroy();
        }
        super.destroy();
    }

    @Override
    public boolean skip(RecommendContext context) {
        return !context.i2iMatchSwitch;
    }

    @Override
    protected List<String> buildKey(RecommendContext context) throws Exception{
        List<String> triggers =  triggerLoader.invoke(context).stream().map(Trigger.Value::getItemId).collect(Collectors.toList());
        context.setTriggers(triggers);
        List<String> keys = context.getTriggers().stream().map(trigger->{
            return new I2i.Key(trigger).buildKey(context.getBizId());
        }).collect(Collectors.toList());
        return keys;
    }

    @Override
    protected List<I2i.Value> parseValue(RecommendContext context,List<String> value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        List<I2i.Value> list = new ArrayList<>(value.size());
        value.forEach(item->{
            I2i.Value i2iValue = I2i.Value.parseValue(item);
            list.add(i2iValue);
        });
        return list;
    }
}

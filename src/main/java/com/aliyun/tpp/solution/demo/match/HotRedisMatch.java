package com.aliyun.tpp.solution.demo.match;

import com.aliyun.tpp.service.redis.RedisConfig;
import lombok.Getter;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.detail.UserDetails;
import com.aliyun.tpp.solution.demo.match.data.Hot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * author: oe date
 * date:   2021/8/30
 * comment:分人群的热门物品召回
 */
public class HotRedisMatch extends RedisMatch<Hot.Value> {

    @Getter
    private UserDetails userDetails;

    public HotRedisMatch(RedisConfig redisConfig, UserDetails userDetails) {
        super(redisConfig);
        this.userDetails = userDetails;
    }

    @Override
    public void init() {
        super.init();
        userDetails.init();
    }

    @Override
    public void destroy() {
        if (userDetails != null) {
            userDetails.destroy();
        }
        super.destroy();
    }

    @Override
    protected List<String> buildKey(RecommendContext context) {
        String rkey = userDetails.queryUser(context.getUserId()).buildRKey();
        String key = new Hot.Key(rkey).buildKey(context.getBizId());
        return Arrays.asList(new String[]{key});
    }

    @Override
    protected List<Hot.Value> parseValue(RecommendContext context, List<String> value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        List<Hot.Value> list = new ArrayList<>(value.size());
        value.forEach(item -> {
            Hot.Value hotValue = Hot.Value.parseValue(item);
            list.add(hotValue);
        });
        return list;
    }
}

/**
 * author: oe
 * date:   2021/12/2
 * comment:方案中使用redis
 */
package com.aliyun.tpp.solution.redis_demo;

import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.service.redis.RedisClient;
import com.aliyun.tpp.service.redis.RedisConfig;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.SolutionProperties;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResultSupport;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/12/2
 * comment:方案中使用redis，场景监控页面可以收集redis调用指标
 */
public class RedisSolution implements RecommendSolution {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSolution.class);

    private RedisClient client;

    @Override
    public void init(SolutionProperties solutionProperties) {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("r-uf*********w9.redis.rds.aliyuncs.com");//vpc内访问
        redisConfig.setPassword("tpp_demo_tester:tpp_demo_tester*****");//账号:密码
        redisConfig.setPort(6379);
        redisConfig.setTimeout(3000);
        redisConfig.setMaxIdleConnectionCount(16);
        redisConfig.setMaxTotalConnectionCount(64);
        try {
            this.client = ServiceProxyHolder.getService(redisConfig, ServiceLoaderProvider.getSuperLoaderEasy(RedisClient.class));//使用proxy可以收集redis调用指标
            this.client.init();
        }catch (Exception e){
            //ignore
            LOGGER.error("init error",e);
            throw new RuntimeException("init error",e);
        }
    }

    @Override
    public void destroy(SolutionProperties solutionProperties) {
        if (client != null) {
            client.destroy();
        }
    }

    @Override
    public RecommendResult recommend(SolutionContext context) throws Exception {
        //request triggerItems=i2itpp_rec_demo00731ff91dfd9fd45d7f3c9413948587
        List<String> keys = Arrays.asList(((String) context.getRequestParams("triggerItems")).split(","));
        //response
        RecommendResultSupport result = new RecommendResultSupport();
        List<String> values = new LinkedList<>();
        result.setRecommendResult(values);
        try {
            for (String key : keys) {
                List<String> value = client.lrange(key, 0, -1)
                        .stream().filter(line -> line != null && line.length() > 0)
                        .collect(Collectors.toList());
                if (value == null || value.isEmpty()) {
                    LOGGER.error("jedis.lrange returns empty!key={}", key);
                } else {
                    values.addAll(value);
                }
            }
        } catch (Exception e) { // 如果不catch 需要上游兜底
            context.getContextLogger().error("recommend error",e);//打印报错日志，会展示在场景->日志分析->用户自定义日志
            LOGGER.error("recommend error",e);//打印报错日志，会展示在场景->日志分析->异常
        } finally {
            //标记空结果，会展示在场景->日志分析->空结果
            result.setEmpty(result.getRecommendResult() == null || result.getRecommendResult().isEmpty());
        }
        return result;
    }
}

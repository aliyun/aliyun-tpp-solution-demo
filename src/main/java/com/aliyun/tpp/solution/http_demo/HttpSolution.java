package com.aliyun.tpp.solution.http_demo;

import com.aliyun.tpp.service.http.HttpClient;
import com.aliyun.tpp.service.http.HttpConfig;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.SolutionProperties;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResultSupport;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
/**
 * author: oe
 * date:   2022/2/25
 * comment:http调用示例，场景监控页面可以收集http调用指标
 */
public class HttpSolution implements RecommendSolution {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSolution.class);

    private HttpClient client;

    @Override
    public void init(SolutionProperties solutionProperties) {
        HttpConfig httpConfig = new HttpConfig();
        try {
            this.client = ServiceProxyHolder.getService(httpConfig, ServiceLoaderProvider.getSuperLoaderEasy(HttpClient.class));
            this.client.init();
        } catch (Exception e) {
            LOGGER.error("init error", e);
            throw new RuntimeException("init error", e);
        }
    }

    @Override
    public void destroy(SolutionProperties solutionProperties) {
        if (client != null){
            client.closeQuietly();
        }
    }

    @Override
    public RecommendResult recommend(SolutionContext context) throws Exception {
        RecommendResult result = new RecommendResultSupport();
        try {
            String response = client.get("http://10.128.0.14/ok/say", StandardCharsets.UTF_8, null);
            if (response == null || response.isEmpty()){
                result.setEmpty(true);//标记空结果，会展示在场景->日志分析->空结果
            }else {
                result.setRecommendResult(Arrays.asList(response));
            }
        }catch (Exception e){ // 如果不catch 需要上游兜底
            context.getContextLogger().error("recommend error",e);//打印报错日志，会展示在场景->日志分析->用户自定义日志
            LOGGER.error("recommend error",e);//打印报错日志，会展示在场景->日志分析->异常
        }
        return result;
    }
}

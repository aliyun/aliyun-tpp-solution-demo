package com.aliyun.tpp.solution.demo.rank;

import com.alibaba.fastjson.JSON;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.predict.PredictClient;
import com.aliyun.tpp.service.predict.PredictConfig;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.service.rec.Rank;
import lombok.Getter;
import shade.protobuf.MessageLite;
import com.aliyun.tpp.solution.demo.data.RecommendContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * author: oe
 * date:   2021/8/31
 * comment:pai-eas排序
 */
public abstract class PaiEasRank<REQUEST extends MessageLite, RESULT> implements Rank<RecommendContext, RESULT> {

    @Getter
    private PredictClient client;

    public PaiEasRank(PredictConfig predictConfig) {
        this.client = ServiceProxyHolder.getService(predictConfig, ServiceLoaderProvider.getSuperLoaderEasy(PredictClient.class));//初始化client
    }

    @PostConstruct
    public void init() {
        this.client.init();
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.shutdown();
        }
    }

    @Override
    public boolean skip(RecommendContext context) {
        return !context.isEasRankSwitch();
    }

    @Override
    public List<RESULT> rank(RecommendContext context) throws Exception {
        checkParams(context);
        REQUEST request = buildRequest(context);
        try {
            byte[] rawResponse = client.predict(request.toByteArray());
            if (rawResponse == null || rawResponse.length <= 0) {
                //记数据日志，用于排查监控
                context.getDataTraceLog().put(this.getClass().getName(), JSON.toJSONString(request));
            }
            return parseResponse(context, rawResponse);
        } catch (Exception e) {
            //记错误日志
            context.getContextLogger().error("predict error", e);
            return null;
        }
    }

    //检查参数
    protected void checkParams(RecommendContext context) {
        if (context.getItemIds() == null || context.getItemIds().isEmpty()) {
            throw new IllegalArgumentException("itemIds is empty");
        }
    }

    //构造request
    protected abstract REQUEST buildRequest(RecommendContext context);

    //解析response
    //空结果应该落日志排查,不允许抛异常
    protected abstract List<RESULT> parseResponse(RecommendContext context, byte[] response) throws Exception;
}

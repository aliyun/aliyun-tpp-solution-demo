package com.aliyun.tpp.solution.predict_demo;


import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.predict.PredictClient;
import com.aliyun.tpp.service.predict.PredictConfig;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.SolutionProperties;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResultSupport;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * author: oe
 * date:   2021/9/6
 * comment:方案中调用pai-eas，场景监控页面可以收集pai-eas调用指标
 */
public class PredictSolution implements RecommendSolution {

    private static final Logger LOGGER = LoggerFactory.getLogger(PredictSolution.class);

    private PredictClient client;

    @Override
    public void init(SolutionProperties solutionProperties) {
        PredictConfig config = new PredictConfig();
        config.setConnectTimeout(1000);
        config.setReadTimeout(1000);
        config.setHost("1726*********423.vpc.cn-shanghai.pai-eas.aliyuncs.com");//VPC内地址，pai-eas必须开通vpc高速直连
        config.setToken("MjU1YjNjNzZl*********A0NzNhZGJjZQ==");
        config.setModel("easy_rec_multi_tower");
        try {
            this.client = ServiceProxyHolder.getService(config, ServiceLoaderProvider.getSuperLoaderEasy(PredictClient.class));//使用proxy可以收集pai-eas调用指标
            this.client.init();
        } catch (Exception e) {
            LOGGER.error("init error", e);
            throw new RuntimeException("init error", e);
        }
    }

    @Override
    public RecommendResult recommend(SolutionContext context) throws Exception {
        RecommendResultSupport result = new RecommendResultSupport();
        try {
            //request itemIds=10103b20efca6825bdecf814790afa7e
            String[] itemIds = ((String) context.getRequestParams("itemIds")).split(",");
            EasyRecProtos.PBRequest request = EasyRecProtos.PBRequest.newBuilder()
                    .putAllUserFeatures(Collections.EMPTY_MAP)
                    .putAllContextFeatures(Collections.EMPTY_MAP)
                    .addAllItemIds(Arrays.asList(itemIds))
                    .build();
            //response
            byte[] rawResponse = client.predict(request.toByteArray());
            List list = parseRarResponse(rawResponse);
            result.setRecommendResult(list);
        } catch (Exception e) { // 如果不catch 需要上游兜底
            context.getContextLogger().error("recommend error",e);//打印报错日志，会展示在场景->日志分析->用户自定义日志
            LOGGER.error("recommend error",e);//打印报错日志，会展示在场景->日志分析->异常
        } finally {
            //标记空结果，会展示在场景->日志分析->空结果
            result.setEmpty(result.getRecommendResult() == null || result.getRecommendResult().isEmpty());
        }
        return result;
    }

    @Override
    public void destroy(SolutionProperties solutionProperties) {
        if (client != null) {
            client.shutdown();
        }
    }

    /**
     * 解析pai-eas的返回结果
     */
    private static List<RankItem> parseRarResponse(byte[] rawResponse) throws Exception {
        EasyRecProtos.PBResponse response = EasyRecProtos.PBResponse.parseFrom(rawResponse);
        Map<String, EasyRecProtos.Results> resultsMap = response.getResultsMap();
        List<RankItem> rankList = new ArrayList<>(resultsMap.size());
        resultsMap.forEach((itemId, result) -> {
            double score = getScore(result);
            RankItem item = new RankItem();
            item.setItemId(itemId);
            item.setRankScore(score);
            rankList.add(item);
        });
        return rankList;
    }

    /**
     * 解析pai-eas的打分
     */
    private static double getScore(EasyRecProtos.Results result) {
        double score = 0.0d;
        if (result != null) {
            score = result.getScores(0);
        }
        return score;
    }
}

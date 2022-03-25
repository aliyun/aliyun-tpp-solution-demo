package com.aliyun.tpp.solution.be_demo;

import com.aliyun.tpp.service.be.BeClient;
import com.aliyun.tpp.service.be.BeConfig;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.SolutionProperties;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResultSupport;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;
import com.aliyuncs.be.client.BeReadRequest;
import com.aliyuncs.be.client.BeResponse;
import com.aliyuncs.be.client.BeResult;
import com.aliyuncs.be.client.protocol.BeBizType;
import com.aliyuncs.be.client.protocol.clause.FilterClause;
import com.aliyuncs.be.client.protocol.clause.FilterOperator;
import com.aliyuncs.be.client.protocol.clause.SingleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * author: oe
 * date:   2021/12/7
 * comment:方案中调用be，场景监控页面可以收集abfs调用指标
 */
public class BeSolution implements RecommendSolution {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeSolution.class);

    private BeClient beClient;

    @Override
    public void init(SolutionProperties solutionProperties) {
        BeConfig beConfig = new BeConfig();
        beConfig.setPassword("tpp*****");
        beConfig.setUsername("tppuser");
        beConfig.setDomain("aime-cn-0********004.aime.aliyuncs.com");
        try {
            //初始化client
            this.beClient = ServiceProxyHolder.getService(beConfig, ServiceLoaderProvider.getSuperLoaderEasy(BeClient.class));
            this.beClient.init();
        } catch (Exception e) {
            LOGGER.error("init error", e);
            throw new RuntimeException("init error", e);
        }
    }

    @Override
    public RecommendResult recommend(SolutionContext context) throws Exception {
        //request 10103b20efca6825bdecf814790afa7e
        List<String> triggers = Arrays.asList(((String) context.getRequestParams("triggerItems")).split(","));
        BeReadRequest x2iRequest = BeReadRequest.builder()
                .bizName("x2i_match")
                .bizType(BeBizType.X2I)
                .returnCount(100)
                .items(triggers)
                .filter(new FilterClause(new SingleFilter(
                        "score",
                        FilterOperator.GT,
                        "'0.1D'")))
                .build();
        //response
        RecommendResultSupport result = new RecommendResultSupport();
        try {
            BeResponse<BeResult> x2iResponse = beClient.query(x2iRequest);
            if (x2iResponse != null && x2iResponse.isSuccess()) {
                BeResult beResult = x2iResponse.getResult();
                int count = beResult.getItemsCount();
                List<Map<String, String>> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    list.add(beResult.getItem(i));
                }
                result.setRecommendResult(list);
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

    @Override
    public void destroy(SolutionProperties solutionProperties) {
        if (beClient != null) {
            beClient.destroy();
        }
    }
}

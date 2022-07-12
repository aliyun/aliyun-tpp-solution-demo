package com.aliyun.tpp.solution.abfs_demo;

import com.aliyun.tpp.service.abfs.ABFSClient;
import com.aliyun.tpp.service.abfs.ABFSConfig;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.SolutionProperties;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResultSupport;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;
import com.taobao.abfs.client.core.PgSessionCtx;
import com.taobao.abfs.client.model.AtomicQuery;
import com.taobao.abfs.client.model.KeyList;
import com.taobao.abfs.client.model.QueryResult;
import com.taobao.abfs.client.model.SingleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * author: oe
 * date:   2021/12/7
 * comment:方案中调用abfs，场景监控页面可以收集abfs调用指标
 */
public class ABFSSolution implements RecommendSolution {
    private static final Logger LOGGER = LoggerFactory.getLogger(ABFSSolution.class);

    private ABFSClient abfsClient;

    @Override
    public void init(SolutionProperties solutionProperties) {
        ABFSConfig abfsConfig = new ABFSConfig();
        abfsConfig.setSrc("tpp_rec_demo");
        abfsConfig.setEndpoint("abfs-cn-2r******z001.abfs.aliyuncs.com");
        abfsConfig.setUserName("tppuser");
        abfsConfig.setUserPasswd("tpp***");
        try {
            //初始化client
            this.abfsClient = ServiceProxyHolder.getService(abfsConfig, ServiceLoaderProvider.getSuperLoaderEasy(ABFSClient.class));
            this.abfsClient.init();
        }catch (Exception e){
            LOGGER.error("init error",e);
            throw new RuntimeException("init error",e);
        }
    }

    @Override
    public RecommendResult recommend(SolutionContext context) throws Exception {
        //request
        String userId = (String) context.getRequestParams("userId");
        KeyList keyList = new KeyList(userId, "tpp_rec_demo");
        AtomicQuery atomicQuery = AtomicQuery.builder()
                .table("tpp_rec_demo_user")
                .keyLists(Arrays.asList(keyList))
                .fields(Arrays.asList("gender", "age_level", "pay_level", "user_tag", "city"))   // 设置返回字段子句，只有设置的这些字段会序列化回到客户端，默认全返回
                .filter("is_cap=1")    // 设置过滤子句，默认无过滤
                .orderby("+age_level;-pay_level") // 设置orderby子句，默认无排序
                .build();
        //response
        RecommendResultSupport result = new RecommendResultSupport();
        List<Map<String, String>> userFeatures = new LinkedList<>();
        result.setRecommendResult(userFeatures);
        try {
            QueryResult queryResult = abfsClient.searchSync(new PgSessionCtx(), atomicQuery);
            if (queryResult != null) {
                List<SingleQueryResult> singleQueryResultList = queryResult.getAllQueryResult();
                if (singleQueryResultList != null && !singleQueryResultList.isEmpty()) {
                    singleQueryResultList.forEach(singleQueryResult -> {
                        int genderIndex = singleQueryResult.getFieldIndex("gender");
                        int ageLevelIndex = singleQueryResult.getFieldIndex("age_level");
                        int payLevelIndex = singleQueryResult.getFieldIndex("pay_level");
                        int userTagIndex = singleQueryResult.getFieldIndex("user_tag");
                        int cityIndex = singleQueryResult.getFieldIndex("city");
                        singleQueryResult.getMatchRecords().forEach(matchRecord -> {
                            Map<String, String> userFeatureRow = new HashMap<>();
                            userFeatureRow.put("gender", matchRecord.getString(genderIndex));
                            userFeatureRow.put("age_level", matchRecord.getString(ageLevelIndex));
                            userFeatureRow.put("pay_level", matchRecord.getString(payLevelIndex));
                            userFeatureRow.put("user_tag", matchRecord.getString(userTagIndex));
                            userFeatureRow.put("city", matchRecord.getString(cityIndex));
                            userFeatures.add(userFeatureRow);
                        });
                    });
                }
            }
        } catch (Exception e) { // 如果不catch 需要上游兜底
            context.getContextLogger().error("recommend error",e);//打印报错日志，会展示在场景->日志分析->用户自定义日志
            LOGGER.error("recommend error",e);//打印报错日志，会展示在场景->日志分析->异常
        }finally {
            result.setEmpty(userFeatures.isEmpty()); //标记空结果，会展示在场景->日志分析->空结果
        }

        return result;
    }

    @Override
    public void destroy(SolutionProperties solutionProperties) {
        if (abfsClient != null) {
            abfsClient.close();
        }
    }
}

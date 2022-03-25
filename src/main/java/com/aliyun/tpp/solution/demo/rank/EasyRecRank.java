package com.aliyun.tpp.solution.demo.rank;

import com.aliyun.tpp.service.abfs.ABFSClient;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.predict.PredictConfig;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.solution.demo.rank.proto.EasyRecProtos;
import com.aliyun.tpp.service.abfs.ABFSConfig;
import com.taobao.abfs.client.core.PgSessionCtx;
import com.taobao.abfs.client.model.AtomicQuery;
import com.taobao.abfs.client.model.KeyList;
import com.taobao.abfs.client.model.QueryResult;
import com.taobao.abfs.client.model.SingleQueryResult;
import shade.protobuf.InvalidProtocolBufferException;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/8/30
 * comment:easyRec排序，基于pai-eas
 */
public class EasyRecRank extends PaiEasRank<EasyRecProtos.PBRequest, ScoreItem> {

    private ABFSClient abfsClient;

    public EasyRecRank(PredictConfig predictConfig, ABFSConfig abfsConfig) {
        super(predictConfig);
        this.abfsClient = ServiceProxyHolder.getService(abfsConfig, ServiceLoaderProvider.getSuperLoaderEasy(ABFSClient.class));//获取client
    }

    @Override
    public void init() {
        super.init();
        abfsClient.init();
    }

    @Override
    public void destroy() {
        if (abfsClient != null) {
            abfsClient.close();
        }
        super.destroy();
    }

    //构造requeset
    protected EasyRecProtos.PBRequest buildRequest(RecommendContext context) {
        Map<String, String> userFeatures = userFeatures(context);
        Map<String, List<String>> contextFeatures = contextFeatures(context);
        if (context.getRequestParams().containsKey("userFeatures")) {
            userFeatures = (Map<String, String>) context.getRequestParams().get("userFeatures");
        }
        if (context.getRequestParams().containsKey("contextFeatures")) {
            contextFeatures = (Map<String, List<String>>) context.getRequestParams().get("contextFeatures");
        }
        Map<String, EasyRecProtos.ContextFeatures> contextFeaturesMap = contextFeatures.entrySet().stream().map(
                entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(),
                        EasyRecProtos.ContextFeatures.newBuilder().addAllFeatures(entry.getValue()).build()
                )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        EasyRecProtos.PBRequest request = EasyRecProtos.PBRequest.newBuilder()
                .putAllUserFeatures(userFeatures)
                .putAllContextFeatures(contextFeaturesMap)
                .addAllItemIds(context.getItemIds())
                .build();
        return request;
    }

    @Override
    protected List<ScoreItem> parseResponse(RecommendContext context, byte[] rawResponse) throws InvalidProtocolBufferException {
        if (rawResponse == null || rawResponse.length <= 0) {
            context.getEmptyTraceLog().put(this.getClass().getName(), "rawResponse is empty");
            return Collections.emptyList();
        }
        EasyRecProtos.PBResponse response = EasyRecProtos.PBResponse.parseFrom(rawResponse);
        Map<String, EasyRecProtos.Results> resultsMap = response.getResultsMap();
        List<ScoreItem> matchList = context.getScoreItemList();
        //matchList exist(online)
        if (matchList != null && !matchList.isEmpty()) {
            matchList.forEach(item -> {
                String itemId = item.getItemId();
                EasyRecProtos.Results result = resultsMap.get(itemId);
                double score = getScore(result);
                item.setRankScore(score);
            });
            return matchList;
        } else {
            //only itemIds(test)
            List<ScoreItem> rankList = new ArrayList<>(resultsMap.size());
            resultsMap.forEach((itemId, result) -> {
                double score = getScore(result);
                ScoreItem item = new ScoreItem();
                item.setItemId(itemId);
                item.setRankScore(score);
                rankList.add(item);
            });
            context.setScoreItemList(rankList);
            return rankList;
        }
    }

    //获取score
    private double getScore(EasyRecProtos.Results result) {
        double score = 0.0d;
        if (result != null) {
            score = result.getScores(0);
        }
        return score;
    }

    // query user feature
    private Map<String, String> userFeatures(RecommendContext context) {
        String userId = context.getUserId();
        if (userId != null) {
            KeyList keyList = new KeyList(userId, context.getBizId());
            AtomicQuery atomicQuery = AtomicQuery.builder()
                    .table("tpp_rec_demo_user")
                    .keyLists(Arrays.asList(keyList))
                    .fields(Arrays.asList("gender", "age_level", "pay_level", "user_tag", "city"))   // 设置返回字段子句，只有设置的这些字段会序列化回到客户端，默认全返回
                    .filter("is_cap=1")    // 设置过滤子句，默认无过滤
                    .orderby("+age_level;-pay_level") // 设置orderby子句，默认无排序
                    .build();
            try {
                QueryResult queryResult = abfsClient.searchSync(new PgSessionCtx(), atomicQuery);
                if (queryResult != null) {
                    List<SingleQueryResult> singleQueryResultList = queryResult.getAllQueryResult();
                    if (!singleQueryResultList.isEmpty()) {
                        Map<String, String> userFeatures = new HashMap<>(singleQueryResultList.size() * 16);
                        singleQueryResultList.forEach(singleQueryResult -> {
                            int genderIndex = singleQueryResult.getFieldIndex("gender");
                            int ageLevelIndex = singleQueryResult.getFieldIndex("age_level");
                            int payLevelIndex = singleQueryResult.getFieldIndex("pay_level");
                            int userTagIndex = singleQueryResult.getFieldIndex("user_tag");
                            int cityIndex = singleQueryResult.getFieldIndex("city");
                            singleQueryResult.getMatchRecords().forEach(matchRecord -> {
                                userFeatures.put("gender", matchRecord.getString(genderIndex));
                                userFeatures.put("age_level", matchRecord.getString(ageLevelIndex));
                                userFeatures.put("pay_level", matchRecord.getString(payLevelIndex));
                                userFeatures.put("user_tag", matchRecord.getString(userTagIndex));
                                userFeatures.put("city", matchRecord.getString(cityIndex));

                            });
                        });
                        return userFeatures;
                    }
                }
            } catch (Exception e) {
                context.getContextLogger().error(this.getClass().getName(), e);
            }
        }
        return Collections.EMPTY_MAP;
    }

    // query context features
    Map<String, List<String>> contextFeatures(RecommendContext context) {
        return Collections.EMPTY_MAP;
    }

}

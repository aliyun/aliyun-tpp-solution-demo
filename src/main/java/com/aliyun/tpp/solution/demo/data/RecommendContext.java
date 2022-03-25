package com.aliyun.tpp.solution.demo.data;

import com.aliyun.tpp.solution.demo.detail.Item;
import com.aliyun.tpp.solution.protocol.ContextLogger;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/8/31
 * comment:推荐上下文
 */
@Data
public class RecommendContext {
    //--------代码中的算法参数和配置--------
    /**
     * 各种开关
     **/
    public boolean i2iMatchSwitch = true;
    public boolean easRankSwitch = true;
    public boolean reRankSwitch = true;//reRank开关
    private boolean defaultSwitch = true;//兜底开关
    /**
     * 召回超时300ms
     */
    public int matchTimeoutMills = 300;
    /**
     * trigger数量上限
     */
    public int triggerMaxNum = 32;
    /**
     * 召回数量上限
     */
    public int matchMaxNum = 100;
    /**
     * 重排分数阈值
     */
    public double filterScore = 0.2d;
    /**
     * 黑商品id，过滤
     */
    public String blackItemId = "b1ab4c4eece434595c13a6fe159adddf";
    /**
     * 打散窗口
     */
    public int diversifyWindow = 2;
    //---------------------一定会有的--------------------

    /**
     * ab config
     */
    public Map<String, Object> abConfigs = new HashMap<>();
    /**
     * 用户ID
     */
    private String userId;
    /**
     * log
     */
    private ContextLogger contextLogger;
    /**
     * request params
     */
    private Map<String, Object> requestParams = new HashMap<>();

    //--------------每一次请求带来的参数和中间结果-------------
    /**
     * 业务id
     */
    private String bizId = "tpp_rec_demo";
    /**
     * 第几页
     */
    private int pageIndex = 1;
    /**
     * 每页数量
     */
    private int pageSize = 100;
    /**
     * trigger item ids 中间结果
     */
    private List<String> triggers;
    /**
     * sim item ids 中间结果
     */
    private List<String> itemIds;
    /**
     * sim ScoreItem 中间结果
     */
    private List<ScoreItem> scoreItemList;
    /**
     * results 中间结果
     */
    private List<Item> itemList;

    /**
     * 跟踪运行过程出现空结果，最总会输出到traceLog
     */
    private Map<String, String> emptyTraceLog = new LinkedHashMap<>(16);
    /**
     * 跟踪运行过程出现的数据(可能比较敏感)，最总会输出到traceLog
     */
    private Map<String, String> dataTraceLog = new LinkedHashMap<>(16);

    /**
     * 构建RecommendContext
     * 如果配置项特别大, 重复比较耗时, 建议进行缓存, 定期更新
     *
     * @param solutionContext
     * @return
     */
    public final static RecommendContext build(SolutionContext solutionContext) {
        RecommendContext context = new RecommendContext();
        context.setContextLogger(solutionContext.getContextLogger());
        //userId
        String userId = (String) solutionContext.getRequestParams("userId");
        context.setUserId(userId == null ? "" : userId);
        //request params
        context.setRequestParams(solutionContext.getRequestParamsMapCopy());
        parseRequestParams(context.getRequestParams(), context);
        //ab config
        solutionContext.abConfigKeySet().forEach(key -> {
            context.getAbConfigs().put(key, solutionContext.getAbConfig(key));
        });
        parseAbConfigs(context.getAbConfigs(), context);
        return context;
    }

    //解析requestParams
    protected static void parseRequestParams(Map<String, Object> requestParams, RecommendContext context) {
        String bizId = (String) requestParams.getOrDefault("bizId", "tpp_rec_demo");
        int pageIndex = (int) requestParams.getOrDefault("pageIndex", 1);
        int pageSize = (int) requestParams.getOrDefault("pageSize", 100);
        context.setBizId(bizId);
        context.setPageIndex(pageIndex);
        context.setPageSize(pageSize);
    }

    //解析abConfig
    protected static void parseAbConfigs(Map<String, Object> abConfigs, RecommendContext context) {
        boolean i2iMatchSwitch = (boolean) abConfigs.getOrDefault("i2iMatchSwitch", true);
        boolean easRankSwitch = (boolean) abConfigs.getOrDefault("easRankSwitch", true);
        boolean reRankSwitch = (boolean) abConfigs.getOrDefault("reRankSwitch", true);
        boolean defaultSwitch = (boolean) abConfigs.getOrDefault("defaultSwitch", true);
        context.setI2iMatchSwitch(i2iMatchSwitch);
        context.setEasRankSwitch(easRankSwitch);
        context.setReRankSwitch(reRankSwitch);
        context.setDefaultSwitch(defaultSwitch);

        int matchTimeoutMills = (int) abConfigs.getOrDefault("matchTimeoutMills", 300);
        int triggerMaxNum = (int) abConfigs.getOrDefault("triggerMaxNum", 32);
        int matchMaxNum = (int) abConfigs.getOrDefault("matchMaxNum", 100);
        double filterScore = (double) abConfigs.getOrDefault("filterScore", 0.2d);
        String blackItemId = (String) abConfigs.getOrDefault("blackItemId", "b1ab4c4eece434595c13a6fe159adddf");
        int diversifyWindow = (int) abConfigs.getOrDefault("diversifyWindow", 2);
        context.setMatchTimeoutMills(matchTimeoutMills);
        context.setTriggerMaxNum(triggerMaxNum);
        context.setMatchMaxNum(matchMaxNum);
        context.setFilterScore(filterScore);
        context.setBlackItemId(blackItemId);
        context.setDiversifyWindow(diversifyWindow);
    }

    //有可能不查详情，所以要判断下
    public List<Item> getResult() {
        if (getItemList() != null && !getItemList().isEmpty()) {
            return getItemList();
        }
        if (getScoreItemList() != null && !getScoreItemList().isEmpty()) {
            return getScoreItemList().stream().map(scoreItem -> Item.convert(scoreItem)).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

}

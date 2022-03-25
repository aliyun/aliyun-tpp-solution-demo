package com.aliyun.tpp.solution.demo;

import com.aliyun.tpp.service.predict.PredictConfig;
import com.aliyun.tpp.solution.demo.cache.FakeCache;
import com.aliyun.tpp.solution.demo.detail.Item;
import com.aliyun.tpp.solution.demo.detail.ItemDetails;
import com.aliyun.tpp.solution.demo.detail.UserDetails;
import com.aliyun.tpp.solution.demo.match.data.Hot;
import com.aliyun.tpp.solution.demo.rank.FinalScoreRank;
import com.aliyun.tpp.solution.demo.rank.proto.EasyRecProtos;
import com.aliyun.tpp.solution.demo.rerank.DiversifyReRank;
import com.aliyun.tpp.solution.demo.rerank.filter.PageFilter;
import com.aliyun.tpp.solution.demo.result.DefaultCacheResult;
import com.aliyun.tpp.service.abfs.ABFSConfig;
import com.aliyun.tpp.service.be.BeConfig;
import com.aliyun.tpp.service.current.NamedThreadPoolExecutorConfig;
import com.aliyun.tpp.service.rec.Rank;
import com.aliyun.tpp.service.rec.ReRank;
import com.aliyun.tpp.service.redis.RedisConfig;
import com.aliyun.tpp.solution.demo.match.*;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.SolutionProperties;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResultSupport;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;
import com.aliyun.tpp.solution.demo.control.RecommendPipeline;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;
import com.aliyun.tpp.solution.demo.match.data.I2i;
import com.aliyun.tpp.solution.demo.rank.PaiEasRank;
import com.aliyun.tpp.solution.demo.rank.EasyRecRank;
import com.aliyun.tpp.solution.demo.rerank.FilterReRank;
import com.aliyun.tpp.solution.demo.rerank.SortReRank;
import com.aliyun.tpp.solution.demo.rerank.filter.ExposureFilter;
import com.aliyun.tpp.solution.demo.trace.TraceLogBuilder;

import java.util.List;

/**
 * create-time:2021/07/06
 * modified-time:2021/09/01
 *
 * @author oe
 * <p>
 * comment:一个推荐方案的完整demo
 * 注意：这里只是个例子，不对您的业务效果负责，请根据自己的需求改造
 *
 * 从真实的业务改编，如有雷同实属巧合
 * match：BE和redis(x2i)
 * rank：pai-eas(multi_tower)
 * rerank：倒序+过滤+打散
 */
public class DemoSolution implements RecommendSolution {

    //recommendPipeline
    private RecommendPipeline recommendPipeline;
    //default
    private DefaultCacheResult defaultCacheResult;

    @Override
    public void init(SolutionProperties solutionProperties) {
        //match
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("r-u******9.redis.rds.aliyuncs.com");//vpc内访问
        redisConfig.setPassword("tpp_demo_tester:tpp_demo_tester*****");//账号:密码
        redisConfig.setPort(6379);
        redisConfig.setTimeout(3000);
        redisConfig.setMaxIdleConnectionCount(16);
        redisConfig.setMaxTotalConnectionCount(64);
        RedisConfig i2iConfig = redisConfig, hotConfig = redisConfig, triggerConfig = redisConfig;
        try {
            //i2i
            TriggerLoader triggerLoader = new TriggerLoader(triggerConfig);
            RedisMatch<I2i.Value> i2iRedisMatch = new I2iRedisMatch(i2iConfig, triggerLoader);
            BeConfig beConfig = new BeConfig();
            beConfig.setPassword("tpp*****");
            beConfig.setUsername("tppuser");
            beConfig.setDomain("aime-cn-0******004.aime.aliyuncs.com");
            BeMatch<I2i.Value> i2iBeMatch = new I2iBeMatch(beConfig, triggerLoader);
            //hot
            RedisMatch<Hot.Value> hotMatch = new HotRedisMatch(hotConfig, new UserDetails());
            //parallel
            NamedThreadPoolExecutorConfig namedThreadPoolExecutorConfig = new NamedThreadPoolExecutorConfig("MatchParallel-pool");
            namedThreadPoolExecutorConfig.setCorePoolSize(3);
            namedThreadPoolExecutorConfig.setMaximumPoolSize(3);
            MatchParallel matchParallel = new MatchParallel(namedThreadPoolExecutorConfig);
            matchParallel.addStep(hotMatch);
            matchParallel.addStep(i2iRedisMatch);
            matchParallel.addStep(i2iBeMatch);
            //rank
            ABFSConfig abfsConfig = new ABFSConfig();
            abfsConfig.setSrc("tpp_rec_demo");
            abfsConfig.setEndpoint("abfs-cn-2r******z001.abfs.aliyuncs.com");
            abfsConfig.setUserName("tppuser");
            abfsConfig.setUserPasswd("tpp***");
            PredictConfig predictConfig = new PredictConfig();
            predictConfig.setHost("1726*********423.vpc.cn-shanghai.pai-eas.aliyuncs.com");//VPC内地址，eas必须开通vpc高速直连
            predictConfig.setToken("MjU1YjNjNzZ***NzNhZGJjZQ==");
            predictConfig.setModel("easy_rec_multi_tower");
            PaiEasRank<EasyRecProtos.PBRequest, ScoreItem> paiEasRank = new EasyRecRank(predictConfig, abfsConfig);
            Rank<RecommendContext, ScoreItem> finalScoreRank = new FinalScoreRank();
            //reRank
            ReRank<RecommendContext, ScoreItem> sortReRank = new SortReRank();
            PageFilter pageFilter = new PageFilter(new FakeCache());
            ExposureFilter exposureFilter = new ExposureFilter(new FakeCache());
            ReRank<RecommendContext, ScoreItem> filterReRank = new FilterReRank(pageFilter, exposureFilter);
            ReRank<RecommendContext, ScoreItem> diversifyReRank = new DiversifyReRank();
            //details
            ItemDetails itemDetails = new ItemDetails();
            //推荐pipeline
            recommendPipeline = new RecommendPipeline();
            recommendPipeline.addStep(matchParallel);
            recommendPipeline.addStep(paiEasRank);
            recommendPipeline.addStep(finalScoreRank);
            recommendPipeline.addStep(sortReRank);
            recommendPipeline.addStep(filterReRank);
            recommendPipeline.addStep(diversifyReRank);
            recommendPipeline.addStep(itemDetails);
            recommendPipeline.init();
            //兜底
            defaultCacheResult = new DefaultCacheResult();
            defaultCacheResult.init();
        } catch (Exception e) {
            throw new RuntimeException("init error", e);
        }
    }

    @Override
    public void destroy(SolutionProperties solutionProperties) {
        recommendPipeline.destroy();
        defaultCacheResult.destroy();
    }

    @Override
    public RecommendResult recommend(SolutionContext rawContext) throws Exception {
        // context and result
        RecommendContext context = RecommendContext.build(rawContext);
        RecommendResult result = new RecommendResultSupport();
        try {
            //match rank reRank detail trace
            recommendPipeline.invoke(context);
        } catch (Exception e) { // 如果不catch 需要上游兜底
            //打印报错日志，会展示在场景->日志分析->用户自定义日志
            context.getContextLogger().error(this.getClass().getName(),e);
        } finally {
            //result
            List<Item> itemList = context.getResult();
            if (itemList == null || itemList.isEmpty()) {
                //标记空结果，会展示在场景->日志分析->空结果
                result.setEmpty(true);
                //兜底逻辑,返回兜底物品
                if (context.isDefaultSwitch()) {
                    itemList = defaultCacheResult.getDefaultItems();
                }
            }
            result.setRecommendResult(itemList);//推荐结果
            result.setAttribute("emptyTraceLog", TraceLogBuilder.buildEmptyLog(context));//空结果
            result.setAttribute("dataTraceLog", TraceLogBuilder.buildDataLog(context));//排查数据
            return result;
        }
    }
}

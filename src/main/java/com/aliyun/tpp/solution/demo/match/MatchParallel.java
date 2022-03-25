package com.aliyun.tpp.solution.demo.match;

import com.aliyun.tpp.service.control.Parallel;
import com.aliyun.tpp.service.current.NamedThreadPoolExecutorConfig;
import com.aliyun.tpp.service.step.Step;
import com.aliyun.tpp.service.utils.Utils;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/9/3
 * comment:多路召回并发，这里主要是hot和i2i并发
 */
public class MatchParallel<STEP extends Step<RecommendContext, List<ScoreItem>>> extends Parallel<STEP, RecommendContext, List<ScoreItem>> {

    public MatchParallel(NamedThreadPoolExecutorConfig config) {
        super(config);
    }

    @Override
    protected List<ScoreItem> getResult(RecommendContext context, Map<String, Future<List<ScoreItem>>> map) throws Exception {
        map.forEach((className, future) -> {
            try {
                List<ScoreItem> one = future.get(context.getMatchTimeoutMills(), TimeUnit.MILLISECONDS);//获取一路召回
                if (one != null && !one.isEmpty()) { //合并多路召回:之前的所有召回+这一路召回
                    List<ScoreItem> list = Utils.union(context.getScoreItemList(), one);
                    context.setScoreItemList(list);
                } else { // 这一路召回为空，记录一下方便排查
                    context.getEmptyTraceLog().put(className, "return empty");
                }
            } catch (InterruptedException e) { //这一路召回执行中被打断
                context.getContextLogger().error(className, e);
            } catch (ExecutionException e) { //这一路召回执行时异常
                context.getContextLogger().error(className, e);
            } catch (TimeoutException e) { //这一路召回超时
                context.getContextLogger().error(className, e);
            } catch (Exception e) {  //其它异常
                context.getContextLogger().error(className, e);
            }
        });
        List<ScoreItem> list = context.getScoreItemList();
        if (list == null || list.isEmpty()) {
            context.getEmptyTraceLog().put(this.getClass().getName(), "return empty");
        } else {
            context.setItemIds(list.stream().map(ScoreItem::getItemId).distinct().collect(Collectors.toList()));
        }
        return list;
    }
}

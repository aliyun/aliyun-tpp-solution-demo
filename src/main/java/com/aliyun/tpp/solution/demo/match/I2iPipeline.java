package com.aliyun.tpp.solution.demo.match;

import com.aliyun.tpp.service.control.Pipeline;
import com.aliyun.tpp.service.step.Step;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;
import com.aliyun.tpp.solution.demo.match.data.I2i;
import com.aliyun.tpp.solution.demo.match.data.Trigger;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/9/3
 * comment:包含trigger查询和i2i召回的流水线，他两有先后关系
 */
public class I2iPipeline<STEP extends Step<RecommendContext, List<RESULT>>, RESULT extends ScoreItem> extends Pipeline<STEP, RecommendContext, List<RESULT>> {

    @Override
    protected List<RESULT> stepResult(RecommendContext context, List<RESULT> result) {
        if (result == null || result.isEmpty()) {
            context.getEmptyTraceLog().put(this.getClass().getName(), "match_return_empty");
        }
        RESULT one = result.get(0);
        if (one instanceof Trigger.Value) {
            List<String> triggers = result.stream()
                    .sorted(Comparator.comparingDouble(ScoreItem::getMatchScore).reversed())
                    .map(ScoreItem::getItemId).collect(Collectors.toList())
                    .subList(0, result.size() < context.triggerMaxNum ? result.size() : context.triggerMaxNum);
            context.setTriggers(triggers);
        } else if (one instanceof I2i.Value) {
            context.setScoreItemList((List<ScoreItem>) result);
        }
        //如果召回阶段商品数较多, 这里建议先做一次截断, 丢弃低质item, 降低 rank 压力
        return result;
    }
}

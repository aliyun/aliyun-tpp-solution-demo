package com.aliyun.tpp.solution.demo.rerank;

import com.aliyun.tpp.solution.demo.rerank.filter.PageFilter;
import com.aliyun.tpp.service.rec.ReRank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;
import com.aliyun.tpp.solution.demo.rerank.filter.ExposureFilter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/8/31
 * comment:过滤这些内容，阈值+黑+曝光+分页
 * 注意：这里只是个例子，不对您的业务效果负责，请根据自己的需求改造
 */
@AllArgsConstructor
public class FilterReRank implements ReRank<RecommendContext, ScoreItem> {

    @Getter
    private PageFilter pageFilter;

    @Getter
    private ExposureFilter exposureFilter;

    @PostConstruct
    public void init() {
        pageFilter.init();
        exposureFilter.init();
    }

    @PreDestroy
    public void destroy() {
        pageFilter.close();
        exposureFilter.close();
    }

    @Override
    public boolean skip(RecommendContext context) {
        return !context.reRankSwitch;
    }

    @Override
    public List<ScoreItem> reRank(RecommendContext context) {
        List<ScoreItem> list = context.getScoreItemList();
        if (list == null || list.isEmpty()) {//没有就不计算
            context.getEmptyTraceLog().put(this.getClass().getName(), "scoreItemList is empty");
            return list;
        }
        //查询曝光+分页缓存
        int pageIndex = context.getPageIndex();
        int pageSize = context.getPageSize();
        List<String> blackIds = new ArrayList<String>(1) {{
            add(context.blackItemId);
        }};
        List<String> pageFilterList = pageFilter.get(context.getBizId(), pageIndex, blackIds);
        List<String> exposureFilterList = exposureFilter.get(context.getBizId(), blackIds);
        //过滤 阈值+黑+曝光+分页
        List<ScoreItem> filterList = list.stream().filter(scoreItem ->
                scoreItem.getFinalScore() > context.filterScore &&
                        !scoreItem.getItemId().equals(context.blackItemId) &&
                        !pageFilterList.contains(scoreItem.getItemId()) &&
                        !exposureFilterList.contains(scoreItem.getItemId())
        ).collect(Collectors.toList());
        //再过滤一些没查到的数据
        List<ScoreItem> reFilterList = pageFilter.missingFilter(filterList, pageSize).stream().distinct().collect(Collectors.toList());
        context.setScoreItemList(reFilterList);
        //曝光+分页 本次结果保存
        List<String> reFilterItemIds = reFilterList.stream().map(ScoreItem::getItemId).collect(Collectors.toList());
        exposureFilterList.addAll(reFilterItemIds);
        pageFilter.put(context.getBizId(), pageIndex, reFilterItemIds, TimeUnit.DAYS.toMillis(1));
        exposureFilter.put(context.getBizId(), exposureFilterList, TimeUnit.DAYS.toMillis(3));
        return reFilterList;
    }
}

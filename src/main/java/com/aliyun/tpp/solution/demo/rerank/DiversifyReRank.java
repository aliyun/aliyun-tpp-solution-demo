package com.aliyun.tpp.solution.demo.rerank;

import com.aliyun.tpp.service.rec.ReRank;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/9/1
 * comment: 打散截断重排
 * 注意：这里只是个例子，不对您的业务效果负责，请根据自己的需求改造
 */
public class DiversifyReRank implements ReRank<RecommendContext, ScoreItem> {

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
        List<ScoreItem> diversifyList = DiversifyReRank.diversify(list, context.diversifyWindow);
        context.setScoreItemList(diversifyList);
        return diversifyList;
    }


    /**
     * 打散
     *
     * @param list   待打散的数据
     * @param window 打散窗口，窗口内必须属于不同分组
     */
    private static List<ScoreItem> diversify(List<ScoreItem> list, int window) {
        if (list != null && window > 0 && window < list.size()) {
            //分组
            int total = list.size();
            Map<String, List<ScoreItem>> diversifyMap = list.stream().collect(Collectors.groupingBy(w -> w.getCatId()));
            if (diversifyMap.keySet().size() < window) {
                throw new RuntimeException("can not diversify");
            }
            //打散
            List<ScoreItem> returnList = new LinkedList<>();
            while (returnList.size() < total) {
                Iterator<String> keyIterator = diversifyMap.keySet().iterator();
                while (keyIterator.hasNext()) {
                    String key = keyIterator.next();
                    List<ScoreItem> value = diversifyMap.get(key);
                    if (value == null || value.isEmpty()) {
                        keyIterator.remove();
                        if (diversifyMap.keySet().size() < window) {
                            //剩下的打不散了
                            return returnList;
                        }
                    }
                    Iterator<ScoreItem> iterator = value.iterator();
                    if (iterator.hasNext()) {
                        ScoreItem item = iterator.next();
                        returnList.add(item);
                        iterator.remove();
                    }
                }
            }
            return returnList;
        } else {
            return list;
        }
    }


}

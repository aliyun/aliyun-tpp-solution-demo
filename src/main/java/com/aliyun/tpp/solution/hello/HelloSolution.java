package com.aliyun.tpp.solution.hello;

import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.SolutionProperties;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResultSupport;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;

import java.util.*;
/**
 * author: oe
 * date:   2021/9/6
 * comment:最简单的solution,没调用任何Client
 */
public class HelloSolution implements RecommendSolution {

    @Override
    public void init(SolutionProperties solutionProperties) {
    }

    @Override
    public RecommendResult recommend(SolutionContext context) throws Exception {
        Map<String, String> abConfigMap = new LinkedHashMap<>();
        context.abConfigKeySet().forEach(key -> {
            String value = context.getAbConfig(key);
            abConfigMap.put(key, value);
        });
        RecommendResult result = new RecommendResultSupport();
        Map<String, Object> map = new TreeMap<>();
        map.put("requestParams", context.getRequestParamsMapCopy());
        map.put("abConfigs", abConfigMap);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        result.setRecommendResult(list);
        result.setAttribute("HelloSolution", "hello hello hello");
        return result;
    }

    @Override
    public void destroy(SolutionProperties solutionProperties) {
    }
}

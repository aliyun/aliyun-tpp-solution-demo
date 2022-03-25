package com.aliyun.tpp.solution.demo.trace;

import com.aliyun.tpp.solution.demo.data.RecommendContext;

/**
 * author: oe
 * date:   2021/9/2
 * @Comment 构造出跟踪日志，例如 空结果、需要排查的数据
 */
public class TraceLogBuilder {

    public static final int MAX_LOG_SIZE = 20 * 1024;//20K个字符

    //构造空结果日志
    public static String buildEmptyLog(RecommendContext context) {
        StringBuilder sb = new StringBuilder("empty-trace-log\n");
        context.getEmptyTraceLog().forEach((key, value) -> {
            sb.append(key).append(":").append(value).append(".\n");
        });
        if (sb.length() > MAX_LOG_SIZE) {
            sb.delete(MAX_LOG_SIZE, sb.length());
        }
        return sb.toString();
    }

    //构造要排查的数据
    public static String buildDataLog(RecommendContext context) {
        StringBuilder sb = new StringBuilder("data-trace-log\n");
        context.getDataTraceLog().forEach((key, value) -> {
            sb.append(key).append(":").append(value).append(".\n");
        });
        if (sb.length() > MAX_LOG_SIZE) {
            sb.delete(MAX_LOG_SIZE, sb.length());
        }
        return sb.toString();
    }
}

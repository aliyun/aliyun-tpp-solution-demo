package com.aliyun.tpp.solution.demo.match.data;

import lombok.*;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

import java.util.Random;

/**
 * author: oe
 * date:   2021/8/30
 * comment:trigger的key和value
 */
@Data
public class Trigger {

    private Key key;
    private Value value;

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Key {
        private String userId;

        public Key(String userId) {
            this.userId = userId;
        }

        public String buildKey(String bizId) {
            return String.format("%s\u0001%s\u0001%s", MatchType.trigger, bizId, userId);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Value extends ScoreItem {
        private String bhvTime;

        public Value(String itemId, int matchRank, double matchScore, String bhvTime) {
            super(itemId,
                    CAT[(new Random().nextInt(CAT.length))],
                    MatchType.trigger.name(),
                    matchRank,
                    matchScore,
                    0.0,
                    0.0);
            this.bhvTime = bhvTime;
        }

        //b1ab4c4eece434595c13a6fe159adddf\u000220210821085939\u00020.6104371954397761\u00022
        public static Value parseValue(String str) {
            if (str == null || str.isEmpty()) {
                throw new IllegalArgumentException("str is empty");
            }
            String[] fields = str.split("\u0002");
            if (fields.length < 4) {
                throw new IllegalArgumentException("fields.length<4");
            }
            Value value = new Value(fields[0],
                    Integer.valueOf(fields[3] == null ? "-1" : fields[3]),
                    Double.valueOf(fields[2] == null ? "0.0" : fields[2]),
                    fields[1]);
            return value;
        }
    }
}

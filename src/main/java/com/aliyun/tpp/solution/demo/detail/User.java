package com.aliyun.tpp.solution.demo.detail;

import lombok.*;

/**
 * author: oe
 * date:   2021/8/30
 * comment:用户信息
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@EqualsAndHashCode
public class User {
    private String userId;
    private String gender;//性别
    private String ageLevel;//年龄层级
    private String payLevel;//购买力层级

    public String buildRKey() {
        return String.format("%s_%s_%s", gender, ageLevel, payLevel);
    }

}

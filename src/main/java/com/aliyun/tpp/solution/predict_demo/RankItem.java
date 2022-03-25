package com.aliyun.tpp.solution.predict_demo;

import lombok.*;

import java.io.Serializable;

/**
 * author: oe
 * date:   2021/8/31
 * comment:排序item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RankItem implements Serializable {
    private String itemId;
    private double rankScore = 0;


}

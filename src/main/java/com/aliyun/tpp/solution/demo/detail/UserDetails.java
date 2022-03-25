package com.aliyun.tpp.solution.demo.detail;

import com.aliyun.tpp.service.step.Step;
import com.aliyun.tpp.solution.demo.data.RecommendContext;

import java.util.Random;

/**
 * author: oe
 * date:   2021/8/30
 * comment:查询user详情
 */
public class UserDetails implements Step<RecommendContext, User> {
    /**
     * 查询user详情
     */
    public User queryUser(String userId) {
        //mock 这里可以改成真实的详情查询
        Random random = new Random();
        int genderIndex = random.nextBoolean() ? 1 : 0;//F or M
        int ageLevelIndex = random.nextInt(9);//0~8
        int payLevelIndex = random.nextInt(7) + 1;//1~7
        User user = new User();
        user.setUserId(userId);//16187b9da496bb6e0ed0d028a09fbbdf
        user.setGender(gender[genderIndex]);
        user.setAgeLevel(String.valueOf(ageLevelIndex));
        user.setPayLevel(String.valueOf(payLevelIndex));
        return user;
    }

    private static final String[] gender = new String[]{"F", "M"};

    @Override
    public User invoke(RecommendContext context) throws Exception {
        String userId = context.getUserId();
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId is empty");
        }
        return queryUser(context.getUserId());
    }
}

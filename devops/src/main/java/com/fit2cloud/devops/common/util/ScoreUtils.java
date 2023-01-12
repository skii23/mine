package com.fit2cloud.devops.common.util;

public class ScoreUtils{
    private Integer calData = 60; // 保留小数后三位

    public void addSuccessRate(Integer rate){
        if (rate >= 0 && rate <= 100) {
            calData = (calData + rate)/2;
        }
    }

    public void addFailRate(Integer rate){
        if (rate >= 0 && rate <= 100) {
            calData = (calData + 100 - rate)/2;
        }
    }

    public void addFailRate(float rate){
        if (rate > 1) {
            return;
        }
        Integer rateInt = (int)(rate * 100);
        addFailRate(rateInt);
    }

    public void addSuccessRate(float rate){
        if (rate > 1) {
            return;
        }
        Integer rateInt = (int)(rate * 100);
        addSuccessRate(rateInt);
    }
    /*
     * 期望的预期指标，超过预期指标越多，则越好
     * count： 指标实际值
     * targetCount： 指标期望值
     * rate： 该指标所占的比重， 1-99
     * */
    public void addExpactTargetCount(Long count, Long targetCount, Integer rate){
        if (rate < 0 || rate > 100) {
            return;
        }
        if (count >= targetCount) {
            calData = (calData + rate)%101;
        } else {
            Long countRate = (targetCount - count)*100L/targetCount;
            calData = (calData + countRate.intValue()*rate/100)%101;
        }
    }

    /*
     * 警戒阈值指标，超过警戒指标越多，则越查
     * count： 指标实际值
     * targetCount： 指标期望值
     * rate： 该指标所占的比重， 1-99
     * */
    public void addWarnTargetCount(Long count, Long targetCount, Integer rate){
        if (rate < 0 || rate > 100) {
            return;
        }
        if (count >= targetCount) {
            calData = (calData > rate)?(calData - rate): 0;
        } else {
            Long countRate = (targetCount - count)*100L/targetCount;
            Integer countScore = countRate.intValue()*rate/100;
            calData = (calData > countScore)?(calData - countScore): 0;
        }

    }

    public float getScore() {
        modifyScore();
        Integer outScore = (calData/2); // (5*66*0.01*10)
        Integer growUp = (outScore%5) > 0 ?1:0;
        outScore = (outScore/5 + growUp)*5;
        return (float)(outScore*0.1);
    }
    
    public Double getScoreDouble() {
        return Double.valueOf(getScore());
    }


    private void modifyScore() {
        calData = (calData > 100) ?100:calData;
    }
}



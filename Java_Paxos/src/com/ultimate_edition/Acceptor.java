package com.ultimate_edition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiezhou
 * @CalssName: Acceptor
 * @Package com.paxos
 * @Description: 接受者
 * @date 2020/6/24/16:08
 */
public class Acceptor {
    /**
     * 接受者的id（唯一标识）
     */
    int id;

    /**
     * 提案Map类型,key 为提案编号，value提案值
     */
    private Map<Integer, String> proporsal = new HashMap<>();

    /**
     * 接收过的最大提案编号N
     */
    int resN;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<Integer, String> getProporsal() {
        return proporsal;
    }

    public void setProporsal(Map<Integer, String> proporsal) {
        this.proporsal = proporsal;
    }

    public int getResN() {
        return resN;
    }

    public void setResN(int resN) {
        this.resN = resN;
    }


    /**
     * @param proposerNumber 提案编号
     */
    public synchronized String prepareReq(int proposerNumber) {
        int maxNumber = 0;
        String maxProposer = "";
        if (proposerNumber < this.resN) { //不响应
            System.out.println("当前提案编号 proposerN: " + proposerNumber + "小于 已经接受的提案编号 this.resN:  " + resN);
            return null;
        } else {
            this.resN = proposerNumber;
            //响应pok
            Map<Integer, String> map = this.proporsal;
            if (map.size() > 0) {
                for (Map.Entry<Integer, String> entry : map.entrySet()) {
                    if (proposerNumber > entry.getKey() && entry.getKey() > maxNumber) {
                        maxNumber = entry.getKey();
                        maxProposer = entry.getValue();
                    }
                }
                return maxNumber + "," + maxProposer;
            }
            return "null,null";
        }
    }

    /**
     * 第二阶段的 accept请求
     */
    public synchronized String acceptReq(int k, String v) {
        if (k >=this.resN) {
            Map<Integer, String> var = this.getProporsal();
            var.put(k, v);
            return "aok";
        }
        return "no";
    }
}

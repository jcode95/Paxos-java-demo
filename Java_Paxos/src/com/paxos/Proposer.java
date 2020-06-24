package com.paxos;

import java.util.Map;

/**
 * @author jiezhou
 * @CalssName: Proposer
 * @Package com.paxos
 * @Description: 提议者
 * @date 2020/6/24/16:08
 */
public class Proposer {
    /**
     * 提议者id(唯一标识)
     */
    private int id;

    /**
     *  提案Map类型,key 为提案编号，value提案值
     */
    private Map<Integer,String> proporsal;

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


}

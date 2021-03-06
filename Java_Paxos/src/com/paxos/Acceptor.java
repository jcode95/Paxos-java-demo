package com.paxos;

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
     *  接受者的id（唯一标识）
     */
    int id;

    /**
     *  提案Map类型,key 为提案编号，value提案值
     */
    private Map<Integer,String> proporsal;

    /**
     *  接收过的最大提案编号N
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
     * @param proposerN 提案编号
     *
     */
    public Map<Integer, String> prepareReq(int proposerN ){
        if(proposerN<this.resN){ //不响应
            System.out.println("proposerN:"+proposerN+",this.resN"+resN);
            return null;
        }else{
            this.resN=proposerN;
            //响应pok
            return this.proporsal;
        }
    }

    /**
     *  第二阶段的 accept请求
     */
    public String acceptReq(Map<Integer,String> map){
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getKey()>=this.resN){
                this.setProporsal(map);
                return "aok";
            }
        }
        return "no";
    }
}

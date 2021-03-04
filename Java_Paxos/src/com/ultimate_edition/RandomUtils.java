package com.ultimate_edition;

import com.paxos.Common;

import java.util.Random;

/**
 * @author jiezhou
 * @CalssName: RandomUtils
 * @Package com.paxos
 * @Description: 随机数(模拟由于网络通信挂掉的提交者或者接受者的id)
 * @date 2020/6/24/16:22
 */
public class RandomUtils {

    public static int randomAcceptorId(){
        Random random = new Random();
        int id = random.nextInt(Common.ACCEPTOR_COUNT)+1;
        System.out.println(id);
        return id;
    }
    public static int randomProposerId(){
        Random random = new Random();
        int id = random.nextInt(Common.PROPOSER_COUNT)+1;
        return id;
    }

   /* public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            randomProposerId();
        }

    }*/
}

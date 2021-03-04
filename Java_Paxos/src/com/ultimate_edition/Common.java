package com.ultimate_edition;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiezhou
 * @CalssName: common
 * @Package com.paxos
 * @Description: 公有的数据类
 * @date 2020/6/24/16:18
 */
public class Common {
    /**
     * 提交者数量
     */
    public static final int PROPOSER_COUNT = 3;


    /**
     *  接受者数量
     */
    public static final int ACCEPTOR_COUNT = 5;

    /**
     * 全局提案编号（初始值为1）
     */
    public static AtomicInteger proposerN=new AtomicInteger(0);







}

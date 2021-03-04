package com.ultimate_edition;


import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiezhou
 * @CalssName: Test
 * @Package com.paxos
 * @Description: 测试
 * @date 2020/6/24/16:15
 */
public class Test {
    private static CountDownLatch cdl = new CountDownLatch(1);
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    //接受者集合
    private static volatile List<Acceptor> acceptorList = new CopyOnWriteArrayList<>();
    //提交者集合
    private static volatile List<Proposer> proposerList = new CopyOnWriteArrayList<>();

    /**
     * 初始化方法
     */
    public static void init() {
        //初始化接受者 acceptor
        for (int i = 1; i <= Common.ACCEPTOR_COUNT; i++) {
            Acceptor acceptor = new Acceptor();
            acceptor.setId(i);
            acceptor.setResN(0);
            acceptor.setProporsal(new ConcurrentHashMap<>());
            acceptorList.add(acceptor);
        }
        //初始化提交者proposer
        for (int i = 1; i <= Common.PROPOSER_COUNT; i++) {
            Proposer proposer = new Proposer();
            proposer.setId(i);
            proposer.setProporsal(new ConcurrentHashMap<>());
            proposerList.add(proposer);
        }
        cdl.countDown();
    }

    public static void main(String[] args) throws InterruptedException {
        init();
        cdl.await();
        process();

    }

    private static void process() throws InterruptedException {
        int id = RandomUtils.randomProposerId();//宕机id
        for (int i = 0; i < proposerList.size(); i++) {
            Proposer proposer = proposerList.get(i);
            if (id != proposer.getId()) {//使用Random来模拟网络通信阻塞（宕机）
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proposalProcess(proposer);
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        Thread.sleep(4000);
        //宕机的机器重启了
        for (Proposer proposer1 : proposerList) {
            if (proposer1.getId() == id) {//找到宕机的机器
                executorService.execute(new Runnable() {//模拟重启
                    @Override
                    public void run() {
                        try {
                            System.out.println(" 机器id： " + id + " 重启了 ");
                            proposalProcess(proposer1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * 提交提案过程
     *
     * @param proposer
     * @throws InterruptedException
     */
    public static synchronized void proposalProcess(Proposer proposer) throws InterruptedException {
        //1、生成提案
        // 先判断（学习）之前的接受者里面有没有接受之前提议者的提案,没有就自己生成一个提案
        //如果有接受者已经接受了之前提议者的提案，无论自己的提案编号大还是小，都得把自己的提案的value指定为之前的那个提案的value
        int proposerNumber = Common.proposerN.incrementAndGet();//新生成一个提案编号
        if (!chackAccept()) {//没有接受过提案
            ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<Integer, String>();
            map.put(proposerNumber, " 提案 " + proposer.getId());
            proposer.setProporsal(map);
        } else {//学习过程
            //之前有接受者接受过提案,只能乖乖用之前的提案值（也就是Map的value使用之前的提案的）
            String maxProposer = "";
            int maxProposerNumber = 0;
            for (int i = 0; i < acceptorList.size(); i++) {
                Acceptor acceptor = acceptorList.get(i);
                Map<Integer, String> proporsal = acceptor.getProporsal();
                if (proporsal.size() > 0) {
                    for (Map.Entry<Integer, String> entry : proporsal.entrySet()) {
                        if (entry.getKey() >= maxProposerNumber) {
                            maxProposerNumber = entry.getKey();
                            maxProposer = entry.getValue();
                        }
                    }
                }
            }
            Map<Integer, String> map = new ConcurrentHashMap<>();
            map.put(proposerNumber, maxProposer);
            proposer.setProporsal(map);
        }

        //阶段一的 2、prepare请求(这里是对所有的接受者进行请求)
        //统计 prepare请求  pok响应的个数
        AtomicInteger var2 = new AtomicInteger(0);
        String var3 = "";
        List<String> ls = new CopyOnWriteArrayList<>();
        for (Acceptor acceptor : acceptorList) {
            var3 = acceptor.prepareReq(proposerNumber);
            if (var3 != null) {
                ls.add(var3);
                var2.incrementAndGet();
            }
        }
        //判断是否收到超过一半响应(包括一半)
        //阶段2，accept请求
        AtomicInteger aokCount = new AtomicInteger(0);
        Boolean half = chackHalf(Common.ACCEPTOR_COUNT, var2.intValue());
        int maxNumber = 0;
        String maxProposer = proposer.getProporsal().get(proposerNumber);
        if (half) {
            for (String s : ls) {
                if (!s.equals("null,null")) {
                    String[] split = s.split(",");
                    if (Integer.parseInt(split[0]) > maxNumber) {
                        maxNumber = Integer.parseInt(split[0]);
                        maxProposer = split[1];
                    }
                }
            }
            proposer.getProporsal().put(proposerNumber, maxProposer);
        } else {
            proposer.getProporsal().clear();
            proposalProcess(proposer);
        }

        //阶段二的accept请求
        for (Acceptor acceptor : acceptorList) {
            String req = acceptor.acceptReq(proposerNumber, maxProposer);
            if ("aok".equals(req)) {
                aokCount.incrementAndGet();
            }
        }
        //如果过半，V被确定，不过半，重新发起Prepare请求
        Boolean var4 = chackHalf(Common.ACCEPTOR_COUNT, aokCount.intValue());
        if (var4) {
            //输出一下每个acceptor的AcceptV
            for (Acceptor acceptor : acceptorList) {
                String s = acceptor.getProporsal().get(acceptor.getResN());
                System.out.println("  接受者  " + acceptor.getId() + "  接受的最终提案编号 acceptN :  " + acceptor.getResN() + " ,接受的最终 提案是acceptV:  " + s);
            }
            System.out.println(" =================");
            return;//结束
        } else {
            proposer.getProporsal().clear();
            proposalProcess(proposer);
        }

    }

    /**
     * 判断是否超过一半响应
     *
     * @param total
     * @param var1
     * @return true 过半  false 不过半
     */
    public synchronized static Boolean chackHalf(int total, int var1) {
        double var = total / 2;
        if (var > var1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @return false 没有接收提案
     */
    public synchronized static Boolean chackAccept() {
        Boolean res = true;
        for (int i = 0; i < acceptorList.size(); i++) {
            Acceptor acceptor = acceptorList.get(i);
            Map<Integer, String> proporsal = acceptor.getProporsal();
            if (proporsal.size() == 0) {//之前没有接受过提案
                res = false;
                break;
            }
        }
        return res;
    }


}

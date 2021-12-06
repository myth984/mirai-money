package org.example.mirai.plugin.task;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import org.example.mirai.plugin.enity.CaiPiao;
import org.example.mirai.plugin.utils.CaiPiaoUtil;
import org.example.mirai.plugin.utils.MoneyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CaiPiaoOpen implements Job {
    public static final String groupId = "";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("准备开奖!!!!!");
        for (Bot bot : Bot.getInstances()) {
            Group group = bot.getGroup(318195769);
            if (group != null) {
                try {
                    Integer poolMoney = CaiPiaoUtil.getPoolMoney();
                    List<CaiPiao> list = CaiPiaoUtil.getSureCaiPiao();
                    Integer finalNumber = MoneyUtil.generateRandomMoney(1, 100);
                    String msg = String.format("本期中奖号码:%s\n恭喜中奖人员:", finalNumber);
                    list = list.stream().filter(i ->
                            i.getNumber().equals(finalNumber)
                    ).collect(Collectors.toList());
                    if (list == null || list.size() == 0) {
                        msg += "\n无\n奖池已经累计" + poolMoney + "金币";
                        group.sendMessage(msg);
                        return;
                    }
                    Integer allS = list.stream().mapToInt(CaiPiao::getMoney).sum();
                    Map<Long, List<CaiPiao>> map = list.stream().collect(Collectors.groupingBy(CaiPiao::getUserId));
                    for (Long userId : map.keySet()) {
                        String name = group.get(userId).getNameCard();
                        // 分数
                        Integer nowS = map.get(userId).stream().mapToInt(CaiPiao::getMoney).sum();
                        // 比例
                        Integer f = (int) Math.ceil((double) nowS / allS);
                        // 得到的钱
                        Integer dMoney = poolMoney / f;
                        MoneyUtil.addMoney(userId, dMoney);
                        msg += String.format("\n%s: %s / %s = %s", name, poolMoney, f, dMoney);
                    }
                    // 失效所有彩票
                    CaiPiaoUtil.setCaiPiaoFalse();
                    CaiPiaoUtil.setPoolMoney(0);
                    group.sendMessage(msg);

                } catch (Exception e) {
                    group.sendMessage("开奖失败:\n" + e.toString());
                }
                return;
            }
        }
    }
}

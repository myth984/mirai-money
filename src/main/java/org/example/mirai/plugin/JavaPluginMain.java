package org.example.mirai.plugin;

import net.mamoe.mirai.console.data.PluginDataStorage;
import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.MiraiLogger;
import org.example.mirai.plugin.enity.*;
import org.example.mirai.plugin.utils.*;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 使用 Java 请把
 * {@code /src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin}
 * 文件内容改成 {@code org.example.mirai.plugin.JavaPluginMain} <br/>
 * 也就是当前主类全类名
 * <p>
 * 使用 Java 可以把 kotlin 源集删除且不会对项目有影响
 * <p>
 * 在 {@code settings.gradle.kts} 里改构建的插件名称、依赖库和插件版本
 * <p>
 * 在该示例下的 {@link JvmPluginDescription} 修改插件名称，id 和版本等
 * <p>
 * 可以使用 {@code src/test/kotlin/RunMirai.kt} 在 IDE 里直接调试，
 * 不用复制到 mirai-console-loader 或其他启动器中调试
 */

public final class JavaPluginMain extends JavaPlugin {
    public static final JavaPluginMain INSTANCE = new JavaPluginMain();

    private JavaPluginMain() {
        super(new JvmPluginDescriptionBuilder("cn.tui.money", "0.0.6")
                .info("瞎整")
                .build());
    }

    @Override
    public void onEnable() {
        MiraiLogger logger = getLogger();
        getLogger().info("路径分割" + System.getProperty("file.separator"));
        // 设置数据库位置
        try {
            JDBCUtil.setPath(getDataFolderPath().toString());
            // 开始定时任务
            CaiPiaoUtil.startLoop();
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().error(e);
        }
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            //监听群消息
            try {
                handleEvent(g);
            } catch (Exception e) {
                g.getSubject().sendMessage("处理失败\n" + e.toString());
                getLogger().error(e);
            }

        });
//        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
//            //监听好友消息
//            try {
//                handleEvent(f);
//            } catch (Exception e) {
//                f.getFriend().sendMessage("处理失败\n" + e.toString());
//                getLogger().error(e);
//            }
//        });
    }


    public static String LAST_MSG = null;

    // 复读机
    public void fuduji(GroupMessageEvent event) {
        String msg = event.getMessage().serializeToMiraiCode();
        if (msg.equals(LAST_MSG)) {
            // 上一条消息和本条消息一样则复读
            event.getSubject().sendMessage(MiraiCode.deserializeMiraiCode(msg));
            LAST_MSG = null;
        } else {
            LAST_MSG = msg;
        }
    }

    public void handleEvent(GroupMessageEvent event) throws Exception {
        String msg = event.getMessage().contentToString();

        switch (msg) {
            // 开始完全匹配
            case "签到":
                sign(event);
                return;
            case "查询":
                details(event);
                return;
            case "商店":
                shop(event);
                return;
            case "sin":
                setu(event);
                return;
            case "help":
                help(event);
                return;
            case "万径人踪灭":
                noSpeakAll(event);
                return;
            case "割韭菜":
                dubo(event);
                return;
            case "持有彩票":
                queryCaiPiao(event);
                return;
            default:
                // 听不懂洋屁
                try {
                    // 开始 at匹配
                    if (isAtMsg(msg, "查询")) {
                        Long userId = getUserIdByAtMsg(msg);
                        details(event, userId);
                        return;
                    } else if (isAtMsg(msg, "抢劫")) {
                        Long userId = getUserIdByAtMsg(msg);
                        rob(event, userId);
                        return;
                    } else if (isAtMsg(msg, "小胶带")) {
                        Long userId = getUserIdByAtMsg(msg);
                        smallJd(event, userId);
                        return;
                    } else if (isAtMsg(msg, "大胶带")) {
                        Long userId = getUserIdByAtMsg(msg);
                        bigJd(event, userId);
                        return;
                    } else {
                        // 开始正则验证
                        Pattern buyPattern = Pattern.compile("购买\\s(\\d+)");
                        Matcher buyMatcher = buyPattern.matcher(msg);

                        // 买彩票

                        Pattern caiPiaoPattern = Pattern.compile("购买彩票\\s(\\d+)\\s(\\d+)");
                        Matcher caiPiaoMatcher = caiPiaoPattern.matcher(msg);
                        if (buyMatcher.find()) {
                            Integer goodsId = Integer.valueOf(buyMatcher.group(1));
                            buy(event, goodsId);
                            return;
                        } else if (caiPiaoMatcher.find()) {
                            Integer number = Integer.valueOf(caiPiaoMatcher.group(1));
                            Integer money = Integer.valueOf(caiPiaoMatcher.group(2));
                            buyCaiPiao(event, number, money);
                        }
                    }
                    fuduji(event);
                } catch (Exception e) {
                    getLogger().error(e);
                }

                break;
        }
    }

    private Long getUserIdByAtMsg(String msg) {
        String[] tmpArr = msg.split("@");
        return Long.valueOf(tmpArr[1].trim());
    }

    private boolean isAtMsg(String msg, String start) {
        return msg.startsWith(start) && msg.contains("@");
    }


    public void smallJd(GroupMessageEvent event, Long aimId) throws Exception {
        Long sourceId = event.getSender().getId();
        ResultBean resultBean = GoodsUtil.useGoods(sourceId, 4);
        if (resultBean.getSuccess()) {
            noSpeak(event, aimId, 1);
        } else {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    resultBean.getMsg()
            ));
        }
    }

    public void queryCaiPiao(GroupMessageEvent event) throws Exception {
        Long userId = event.getSender().getId();
        List<CaiPiao> list = CaiPiaoUtil.getSureCaiPiao(userId);
        if (list == null || list.size() == 0) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    "暂无持有彩票\n输入 购买彩票 号码(1-100) 金额\n如购买彩票 2 200"
            ));
            return;
        }
        String msg = "持有彩票:";
        Map<Integer, List<CaiPiao>> map = list.stream().collect(Collectors.groupingBy(CaiPiao::getNumber));
        for (Integer number : map.keySet()) {
            Integer money = map.get(number).stream().mapToInt(CaiPiao::getMoney).sum();
            msg += String.format("\n[%s]号彩票 %s金币", number, money);
        }
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                msg
        ));
    }

    public void caiPiaoDetails(GroupMessageEvent event) throws Exception {
        event.getSubject().sendMessage(new QuoteReply(event.getSource())
                .plus("每日 11点 3点 5点 开奖, 一金币一注\n" +
                        "输入 购买彩票 "));
        return;
    }

    public void dubo(GroupMessageEvent event) throws Exception {

        Long userId = event.getSender().getId();

        List<DuboLog> duboLogs = DuboUtil.getToDayDuboLogs(userId);
        if (duboLogs.size() > 10) {
            Integer duboMoney = duboLogs.stream().mapToInt(DuboLog::getMoney).sum();
            event.getSubject().sendMessage(new QuoteReply(event.getSource())
                    .plus(String.format("收手吧,久赌必输")));
            return;
        }


        Integer cardMoney = 50;
        Integer nowMoney = MoneyUtil.getMoney(userId);
        if (nowMoney < cardMoney) {
            event.getSubject().sendMessage("至少需要" + cardMoney + "金币");
            return;
        }
        // 扣除入场费
        nowMoney = nowMoney - cardMoney;
        // 生成随机数
        String msg = String.format("%s进入了赌场,花费%s\n", event.getSender().getNameCard(), cardMoney);
        // 调高输的精光的概率
        Integer score = MoneyUtil.generateRandomMoney(0, 101);
        if (score <= 40) {
            Integer gMoney = MoneyUtil.generateRandomMoney(30, 35);
            msg += String.format("%s,赢了%s", DuboUtil.getPlayName(), gMoney);
            nowMoney = nowMoney + gMoney;
        } else if (score > 40 && score <= 70) {
            Integer gMoney = MoneyUtil.generateRandomMoney(35, 60);
            msg += String.format("%s,赢了%s", DuboUtil.getPlayName(), gMoney);
            nowMoney = nowMoney + gMoney;
        } else if (score > 70 && score <= 90) {
            Integer gMoney = MoneyUtil.generateRandomMoney(60, 80);
            msg += String.format("%s,赢了%s", DuboUtil.getPlayName(), gMoney);
            nowMoney = nowMoney + gMoney;
        } else if (score > 90 && score <= 98) {
            Integer gMoney = MoneyUtil.generateRandomMoney(60, 80);
            msg += String.format("%s,赢了%s", DuboUtil.getPlayName(), gMoney);
            nowMoney = nowMoney + gMoney;
        } else if (score == 99) {
            nowMoney = nowMoney + 500;
            msg += String.format("牛哇,把赌场硬走了,赢了%s", 500);
        } else {
            nowMoney = nowMoney - 500;
            if (nowMoney < 0) {
                nowMoney = 0;
            }
            msg += String.format("出老千被砍了手,失去500金币");
        }
        MoneyUtil.setMoney(userId, nowMoney);
        DuboUtil.addDuboLog(userId, nowMoney);
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                msg
        ));

    }

    public void noSpeakAll(GroupMessageEvent event) throws Exception {
        Long sourceId = event.getSender().getId();
        ResultBean resultBean = GoodsUtil.useGoods(sourceId, 6);
        if (resultBean.getSuccess()) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    String.format("%s使用了万径人踪灭!!!!!", event.getSender().getNameCard())
            ));
            List<User> userList = UserUtils.getAllUser();
            for (User user : userList) {
                noSpeak(event, user.getId(), 10);
            }
        } else {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    resultBean.getMsg()
            ));
        }
    }


    public void bigJd(GroupMessageEvent event, Long aimId) throws Exception {
        Long sourceId = event.getSender().getId();
        ResultBean resultBean = GoodsUtil.useGoods(sourceId, 5);
        if (resultBean.getSuccess()) {
            noSpeak(event, aimId, 5);
        } else {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    resultBean.getMsg()
            ));
        }

    }


    public void noSpeak(GroupMessageEvent event, Long aimId, Integer minute) {
        NormalMember m = event.getGroup().get(aimId);
        if (m.isMuted()) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    String.format("%s 已经被禁言, 道具也不退了", m.getNameCard())
            ));
        } else {
            m.mute(minute * 60);
        }
    }

    public void buy(GroupMessageEvent event, Integer goodsId) throws Exception {
        Long userId = event.getSender().getId();
        ResultBean resultBean = GoodsUtil.buy(userId, goodsId);
        Goods goods = Goods.goodsMap.get(goodsId);
        if (goods != null) {
            if (resultBean.getSuccess()) {
                event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                        String.format("购买[%s]成功  已经放入背包", goods.getName())
                ));
            } else {
                event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                        String.format("购买[%s]失败 %s", goods.getName(), resultBean.getMsg())
                ));
            }
        }
    }

    public void buyCaiPiao(GroupMessageEvent event, Integer number, Integer money) throws Exception {
        Long userId = event.getSender().getId();
        ResultBean resultBean = CaiPiaoUtil.buyCaiPiao(userId, number, money);
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                resultBean.getMsg()
        ));
    }

    public void help(GroupMessageEvent event) throws Exception {
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                "签到\n查询\n商店\nsin\n抢劫 @xxx\n购买 ?\n割韭菜\n" +
                        "持有彩票\n购买彩票 number(1-100) money"
        ));
    }

    public void setu(GroupMessageEvent event) throws Exception {
        Long userId = event.getSender().getId();
        Integer money = MoneyUtil.getMoney(userId);
        if (money < 2) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    String.format("2金币,才能看色图,穷逼不配")
            ));
            return;
        }
        MoneyUtil.setMoney(userId, money - 2);
        URL url = new URL("https://api.nmb.show/xiaojiejie1.php");
        URLConnection uc = url.openConnection();
        InputStream inputStream = uc.getInputStream();
        Contact.sendImage(event.getGroup(), inputStream);
    }


    /**
     * 签到
     *
     * @param event
     */
    public void sign(GroupMessageEvent event) throws Exception {
        Member user = event.getSender();
        Long userId = user.getId();
        Date lastSign = SignUtil.getLastSignDate(userId);
        String msg = null;
        // 是否是第一次签到
        if (lastSign == null) {
            Integer money = MoneyUtil.generateRandomMoney(50, 100);
            msg = String.format("%s 还未签到过\n初次签到成功\n获得%s个金币", user.getNick(), money);
            // 建档
            SignUtil.sign(userId);
            SignUtil.createUser(userId, money);
        } else {
            String dateStr = DateUtil.formatDate(lastSign);
            String nowDateStr = DateUtil.formatDate();
            // 判断今天是否签到过了
            if (nowDateStr.equals(dateStr)) {
                msg = String.format("%s 今天已经签过到了\n你在想peach", user.getNick());
            } else {
                Integer money = MoneyUtil.generateRandomMoney(30, 100);
                msg = String.format("%s 签到成功\n获得%s个金币", user.getNick(), money);
                SignUtil.sign(userId);
                MoneyUtil.addMoney(userId, money);
            }
        }
        // 回复某人消息
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(msg));
    }

    public void details(GroupMessageEvent event, Long userId) throws Exception {
        doDetails(event, userId);
    }


    public void rob(GroupMessageEvent event, Long aimId) throws Exception {
        Member user = event.getSender();
        Long sourceId = user.getId();
        if (SignUtil.getLastSignDate(sourceId) == null) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus("银行没开户,不能抢劫"));
            return;
        }
        ;
        if (sourceId.equals(aimId)) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus("自己抢自己?"));
            return;
        }
        Integer sourceUserMoney = MoneyUtil.getMoney(sourceId);
        Integer aimUserMoney = MoneyUtil.getMoney(aimId);
        if (aimUserMoney == 0) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus("这个穷逼一分钱没有"));
            return;
        }
        List<RobLog> robLogs = RobLogUtil.getToDayRobLogs(sourceId);
        if (robLogs.size() >= 5) {
            Integer robMoney = robLogs.stream().mapToInt(RobLog::getMoney).sum();
            event.getSubject().sendMessage(new QuoteReply(event.getSource())
                    .plus(String.format("收手吧,本日抢劫已上限,今日共抢了%s金币", robMoney)));
            return;
        }
        // 0 - 10
        List<Goods> sourceGoods = GoodsUtil.getHasGoodsIdList(sourceId);
        List<Goods> aimGoods = GoodsUtil.getHasGoodsIdList(aimId);
        Integer sourceValue = MoneyUtil.generateRandomMoney(0, 50);
        Integer aimValue = MoneyUtil.generateRandomMoney(0, 40);
        // 计算抢劫人的分数
        for (Goods goods : sourceGoods) {
            switch (goods.getId()) {
                case 1:
                    sourceValue += 10;
                    break;
                case 2:
                    break;
                case 3:
                    break;
                default:
                    break;
            }
        }
        // 计算被抢劫人的分数
        for (Goods goods : aimGoods) {
            switch (goods.getId()) {
                case 1:
                    break;
                case 2:
                    aimValue += 10;
                    break;
                case 3:
                    aimValue += -100;
                    break;
                default:
                    break;
            }
        }
        String msg = null;
        if (sourceValue >= aimValue) {
            // 抢劫成功
            Integer loseMoney = MoneyUtil.generateRandomMoney(1, 10);
            if (loseMoney > aimUserMoney) {
                loseMoney = aimUserMoney;
            }
            Integer nowMoney = sourceUserMoney + loseMoney;
            msg = String.format("抢劫成功,抢到了%s金币\n当前余额%s金币", loseMoney, nowMoney);
            MoneyUtil.setMoney(sourceId, nowMoney);
            MoneyUtil.setMoney(aimId, aimUserMoney - loseMoney);
            // 存入日志表
            RobLogUtil.addRobLog(sourceId, loseMoney);
        } else {
            // 抢劫失败
            Integer loseMoney = MoneyUtil.generateRandomMoney(1, 10);
            if (loseMoney > sourceUserMoney) {
                loseMoney = sourceUserMoney;
            }
            Integer nowMoney = sourceUserMoney - loseMoney;
            msg = String.format("抢劫失败,你没有打过对方,对方反手抢了你%s金币\n当前余额%s金币", loseMoney, nowMoney);
            MoneyUtil.setMoney(sourceId, nowMoney);
            MoneyUtil.setMoney(aimId, loseMoney + aimUserMoney);
            // 存入日志表
            RobLogUtil.addRobLog(sourceId, -loseMoney);
        }
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(msg));
    }


    public void doDetails(GroupMessageEvent event, Long userId) throws Exception {
        Member user = event.getSender();
        Date lastSign = SignUtil.getLastSignDate(userId);
        Integer money = MoneyUtil.getMoney(userId);
        String dateStr = "";
        if (lastSign == null) {
            dateStr = "还未签到过";
        } else {
            dateStr = DateUtil.formatDate(lastSign);
        }
        String msg = String.format("个人信息:\n金币:%s\n上次签到:%s", money, dateStr);
        List<Goods> goodsList = GoodsUtil.getHasGoodsIdList(userId);
        if (goodsList == null || goodsList.isEmpty()) {
            msg += "\n穷逼一个没有装备";
        } else {
            msg += "\n当前装备:";
            for (Goods goods : goodsList) {
                msg += "\n" + goods.getName();
            }
        }
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(msg));
    }

    public void details(GroupMessageEvent event) throws Exception {
        Member user = event.getSender();
        Long userId = user.getId();
        doDetails(event, userId);
    }

    public void shop(GroupMessageEvent event) throws Exception {
        String msg = "商品列表:";
        for (Goods goods : Goods.allGoods) {
            String name = goods.getName();
            Integer price = goods.getPrice();
            String desc = goods.getDescribe();
            msg += String.format("\n-%s %s %s金币\n\t%s", goods.getId(), name, price, desc);
        }
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(msg));
    }
}

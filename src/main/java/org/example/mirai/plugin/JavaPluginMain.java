package org.example.mirai.plugin;

import io.ktor.http.Url;
import kotlinx.coroutines.CoroutineScope;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;
import org.example.mirai.plugin.enity.Goods;
import org.example.mirai.plugin.enity.ResultBean;
import org.example.mirai.plugin.enity.RobLog;
import org.example.mirai.plugin.enity.User;
import org.example.mirai.plugin.utils.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        super(new JvmPluginDescriptionBuilder("cn.tui.money", "0.0.5")
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
            Connection connection = JDBCUtil.getConnection();
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
                break;
            case "查询":
                details(event);
                break;
            case "商店":
                shop(event);
                break;
            case "sin":
                setu(event);
                break;
            case "help":
                help(event);
                break;
            case "万径人踪灭":
                noSpeakAll(event);
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
                        if (buyMatcher.find()) {
                            Integer goodsId = Integer.valueOf(buyMatcher.group(1));
                            buy(event, goodsId);
                            return;
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

    public void noSpeakAll(GroupMessageEvent event) throws Exception {
        Long sourceId = event.getSender().getId();
        ResultBean resultBean = GoodsUtil.useGoods(sourceId, 6);
        if (resultBean.getSuccess()) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    String.format("%s使用了万径人踪灭!!!!!", event.getSender().getNameCard())
            ));
            event.getGroup().getSettings().setMuteAll(true);
            new Thread(() -> {
                try {
                    Thread.sleep(15 * 60 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                event.getGroup().getSettings().setMuteAll(false);
            }).start();
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

    public void help(GroupMessageEvent event) throws Exception {
        event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                "签到\n查询\n商店\nsin\n抢劫 @xxx\n购买 ?"
        ));
    }

    public void setu(GroupMessageEvent event) throws Exception {
        Long userId = event.getSender().getId();
        Integer money = MoneyUtil.getMoney(userId);
        if (money < 1) {
            event.getSubject().sendMessage(new QuoteReply(event.getSource()).plus(
                    String.format("1金币,才能看色图,穷逼不配")
            ));
            return;
        }
        MoneyUtil.setMoney(userId, money - 1);
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
        Integer aimValue = MoneyUtil.generateRandomMoney(0, 50);
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

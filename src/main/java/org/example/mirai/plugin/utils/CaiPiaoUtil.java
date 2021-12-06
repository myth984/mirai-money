package org.example.mirai.plugin.utils;

import net.mamoe.mirai.console.data.Value;
import org.example.mirai.plugin.enity.CaiPiao;
import org.example.mirai.plugin.enity.Goods;
import org.example.mirai.plugin.enity.ResultBean;
import org.example.mirai.plugin.task.CaiPiaoOpen;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class CaiPiaoUtil {


    public static void startLoop() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(CaiPiaoOpen.class).build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 18 * * ?"))
                .build();
        Scheduler scheduler =  new StdSchedulerFactory().getScheduler();
        //将任务及其触发器放入调度器
        scheduler.scheduleJob(jobDetail, trigger);
        //调度器开始调度任务
        scheduler.start();
    }


    public static ResultBean buyCaiPiao(Long userId, Integer number, Integer money) throws Exception {
        if (number > 100 && number < 1) {
            return ResultBean.error("彩票号码1-100");
        }
        Integer hasMoney = MoneyUtil.getMoney(userId);
        if (hasMoney < money) {
            return ResultBean.error("钱不够买不起,当前余额" + hasMoney);
        }
        MoneyUtil.setMoney(userId, hasMoney - money);
        Connection connection = JDBCUtil.getConnection();
        String sql = "insert into caipiao (user_id,money, date, number, sure) values (?, ?, ?, ?, ?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            pst.setLong(1, userId);
            pst.setInt(2, money);
            pst.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pst.setInt(4, number);
            pst.setInt(5, CaiPiao.SURE_OK);
            pst.execute();
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
        Integer poolMoney = getPoolMoney() + money;
        setPoolMoney(poolMoney);
        return ResultBean.success("购买成功,开奖时按照购买金额的比例,瓜分奖池金额\n当前奖池" + poolMoney + "金币");
    }

    /**
     * 获得全部有效的彩票
     *
     * @return
     */
    public static List<CaiPiao> getSureCaiPiao() throws Exception {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select user_id,money, date, number, sure from caipiao where sure = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            pst.setInt(1, CaiPiao.SURE_OK);
            rs = pst.executeQuery();
            List<CaiPiao> result = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                CaiPiao caiPiao = new CaiPiao();
                caiPiao.setUserId(rs.getLong(1));
                caiPiao.setMoney(rs.getInt(2));
                caiPiao.setDate(rs.getDate(3));
                caiPiao.setNumber(rs.getInt(4));
                caiPiao.setSure(rs.getInt(5));
                result.add(caiPiao);
            }
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            pst.close();
        }
    }

    public static Integer getPoolMoney() throws Exception {
        Connection connection = JDBCUtil.createConnection();
        String sql = "select money from caipiao_pool limit 1";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (Exception e) {
            pst.close();
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            pst.close();
        }
    }

    public static Integer setPoolMoney(Integer money) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "update caipiao_pool set money = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            pst.setInt(1, money);
            pst.executeUpdate();
            return money;
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
    }

    /**
     * 过去全部彩票
     *
     * @param money
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */

    public static void setCaiPiaoFalse() throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "update caipiao set sure = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            pst.setInt(1, CaiPiao.SURE_ERROR);
            pst.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
    }

    /**
     * 获得全部有效的彩票
     *
     * @return
     */
    public static List<CaiPiao> getSureCaiPiao(Long userId) throws Exception {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select user_id,money, date, number, sure from caipiao where sure = ? and user_id = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            pst.setBoolean(1, true);
            pst.setLong(2, userId);
            rs = pst.executeQuery();
            List<CaiPiao> result = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                CaiPiao caiPiao = new CaiPiao();
                caiPiao.setUserId(rs.getLong(1));
                caiPiao.setMoney(rs.getInt(2));
                caiPiao.setDate(rs.getDate(3));
                caiPiao.setNumber(rs.getInt(4));
                caiPiao.setSure(rs.getInt(5));
                result.add(caiPiao);
            }
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            pst.close();
        }
    }
}

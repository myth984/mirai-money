package org.example.mirai.plugin.utils;


import org.example.mirai.plugin.enity.DuboLog;
import org.example.mirai.plugin.enity.DuboLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DuboUtil {
    public static final String[] playNameArr = {
            "玩起了四川麻将",
            "开始了斗地主",
            "和保安下起了象棋",
            "偶遇富婆,被富婆快乐球玩弄",
            "下了一整晚飞行棋",
            "和三个老太太玩了一夜升级",
            "开始了拖拉机"
    };

    public static String getPlayName() {
        int index = (int) (Math.random() * playNameArr.length);
        return playNameArr[index];
    }

    public static List<DuboLog> getToDayDuboLogs(Long userId) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select user_id,money, date from dubo_log " +
                "where user_id = ? and strftime('%Y-%m-%d',datetime(`date`/1000,'unixepoch') ) = strftime('%Y-%m-%d','now')";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            pst.setLong(1, userId);
            rs = pst.executeQuery();
            List<DuboLog> result = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                DuboLog log = new DuboLog();
                log.setUserId(userId);
                log.setMoney(rs.getInt(2));
                log.setDate(rs.getDate(3));
                result.add(log);
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

    public static void addDuboLog(Long userId, Integer money) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "insert into dubo_log (user_id,money, date) values (?, ?, ?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            pst.setLong(1, userId);
            pst.setLong(2, money);
            pst.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pst.execute();
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
    }

    public static Integer getToDayDuboSum(Long userId) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select count(1) from dubo_log" +
                "where user_id = ? and strftime('%Y-%m-%d',datetime(`date`/1000,'unixepoch') ) = strftime('%Y-%m-%d','now')";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            pst.setLong(1, userId);
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
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

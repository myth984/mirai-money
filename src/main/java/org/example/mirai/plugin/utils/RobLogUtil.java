package org.example.mirai.plugin.utils;

import org.example.mirai.plugin.enity.Goods;
import org.example.mirai.plugin.enity.RobLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RobLogUtil {

    public static List<RobLog> getToDayRobLogs(Long userId) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select user_id,money, date from rob_log " +
                "where user_id = ? and strftime('%Y-%m-%d',datetime(`date`/1000,'unixepoch') ) = strftime('%Y-%m-%d','now')";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            pst.setLong(1, userId);
            rs = pst.executeQuery();
            List<RobLog> result = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                RobLog log = new RobLog();
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

    public static void addRobLog(Long userId, Integer money) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "insert into rob_log (user_id,money, date) values (?, ?, ?)";
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

    public static Integer getToDayRobSum(Long userId) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select count(1) from rob_log" +
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

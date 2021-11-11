package org.example.mirai.plugin.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class MoneyUtil {
    private static Random ra = new Random();

    public static Integer generateRandomMoney() {
        return ra.nextInt(100);
    }

    public static Integer generateRandomMoney(Integer max) {
        return ra.nextInt(max);
    }

    public static Integer generateRandomMoney(Integer min, Integer max) {
        return min + ra.nextInt(max - min);
    }

    public static Integer addMoney(Long id, Integer money) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "update `user` set money = ? where id = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            Integer nowMoney = getMoney(id);
            Integer resultMoney = nowMoney + money;
            pst.setInt(1, resultMoney);
            pst.setLong(2, id);
            pst.executeUpdate();
            return resultMoney;
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
    }

    public static Integer setMoney(Long id, Integer money) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "update `user` set money = ? where id = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            pst.setInt(1, money);
            pst.setLong(2, id);
            pst.executeUpdate();
            return money;
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
    }

    public static Integer getMoney(Long id) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select money from user where id = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            pst.setLong(1, id);
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
}

package org.example.mirai.plugin.utils;

import java.sql.*;
import java.util.Date;

public class SignUtil {

    public static void createUser(Long userId, Integer money) throws Exception {
        Connection connection = JDBCUtil.getConnection();
        String sql = "insert into user (id, money) values (?, ?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            pst.setLong(1, userId);
            pst.setInt(2, money);
            pst.execute();
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
    }

    public static Date getLastSignDate(Long id) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select `date` from sign_log where user_id = ? order by date desc limit 1";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            pst.setLong(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getDate(1);
            } else {
                return null;
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

    public static void sign(Long id) throws SQLException, ClassNotFoundException {
        Connection connection = JDBCUtil.getConnection();
        String sql = "insert into sign_log (user_id, date) values (?, ?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            pst.setLong(1, id);
            pst.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            pst.execute();
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
    }
}

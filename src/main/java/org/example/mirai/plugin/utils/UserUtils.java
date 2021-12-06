package org.example.mirai.plugin.utils;

import org.example.mirai.plugin.enity.Goods;
import org.example.mirai.plugin.enity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserUtils {
    public static List<User> getAllUser() throws Exception {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select id, money from user";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            rs = pst.executeQuery();
            List<User> result = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong(1));
                user.setMoney(rs.getLong(2));
                result.add(user);
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

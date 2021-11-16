package org.example.mirai.plugin.utils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtil {
    private static final String ClASS_NAME = "org.sqlite.JDBC";

    private static Connection connection = null;
    private static String DB_PATH = null;
    private static String DB_FILE_NAME = "data.db";

    public static void setPath(String path) throws Exception {
        DB_PATH = path;
        initDataBase();
    }

    private static void initDataBase() throws Exception {
        File file = new File(DB_PATH + File.separator + DB_FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
            System.out.println("文件创建成功");

            Connection connection = getConnection();

            Statement statement = connection.createStatement();
            try {


                // 用户表
                String createSQL = "CREATE TABLE \"user\" (" +
                        "id BIGINT NOT NULL, " +
                        "money INTEGER DEFAULT 0 " +
                        ");";
                statement.execute(createSQL);
                // 签到日志表
                String signSQL = "CREATE TABLE sign_log (" +
                        "user_id BIGINT NOT NULL," +
                        "date DATETIME NOT NULL" +
                        ");";
                statement.execute(signSQL);
                // 商品表
                String goods = "CREATE TABLE goods (\n" +
                        "\tid INTEGER NOT NULL,\n" +
                        "\t\"type\" INTEGER NOT NULL,\n" +
                        "\tname TEXT NOT NULL,\n" +
                        "\tprice INTEGER DEFAULT 0 NOT NULL,\n" +
                        "\t\"describe\" TEXT,\n" +
                        "\tCONSTRAINT NewTable_PK PRIMARY KEY (id)\n" +
                        ");\n";
                statement.execute(goods);
                // 用户商品映射表
                String userGoodsSQL = "CREATE TABLE user_goods_mapping (\n" +
                        "\tuser_id BIGINT NOT NULL,\n" +
                        "\tgoods_id INTEGER NOT NULL\n" +
                        ");\n" +
                        "CREATE UNIQUE INDEX user_goods_mapping_user_id_IDX ON user_goods_mapping (user_id,goods_id);\n";
                statement.execute(userGoodsSQL);
                // 抢劫日志表
                String robLog = "CREATE TABLE rob_log (\n" +
                        "\tuser_id BIGINT NOT NULL,\n" +
                        "\tmoney INTEGER NOT NULL,\n" +
                        "\tdate DATETIME NOT NULL\n" +
                        ");";
                statement.execute(userGoodsSQL);

            } catch (Exception e) {
                throw e;
            } finally {
                statement.close();
            }
        }
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        if (connection == null) {
            connection = createConnection();
        }
        return connection;
    }

    public static Connection createConnection() throws SQLException, ClassNotFoundException {
        Class.forName(ClASS_NAME);
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH + "\\" + File.separator + DB_FILE_NAME);
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        createConnection();
    }
}

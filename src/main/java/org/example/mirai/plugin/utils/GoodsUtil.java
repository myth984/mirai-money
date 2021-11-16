package org.example.mirai.plugin.utils;

import net.mamoe.mirai.utils.MiraiLogger;
import org.example.mirai.plugin.JavaPluginMain;
import org.example.mirai.plugin.enity.Goods;
import org.example.mirai.plugin.enity.ResultBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.example.mirai.plugin.enity.Goods.TYPE_EQUIP;

public class GoodsUtil {
    public static ResultBean buy(Long userId, Integer goodsId) throws Exception {
        List<Goods> allGoods = Goods.allGoods;
        MiraiLogger logger = JavaPluginMain.INSTANCE.getLogger();
        for (Goods goods : allGoods) {
            if (goods.getId().equals(goodsId)) {
                Integer userMoney = MoneyUtil.getMoney(userId);
                logger.info(String.format("用户%s:具有%s,想要购买%s", userId, userMoney, goodsId));
                // 如果钱够则购买成功
                if (userMoney >= goods.getPrice()) {
                    List<Goods> goodsIdList = getHasGoodsIdList(userId);
                    // 判断之前是否购买过 并且不是消耗品
                    if (!goods.isConsumed() && goodsIdList.contains(goods)) {
                        return ResultBean.error("你已经买过了");
                    } else {
                        MoneyUtil.setMoney(userId, userMoney - goods.getPrice());
                        Connection connection = JDBCUtil.getConnection();
                        String sql = "insert into user_goods_mapping (user_id, goods_id) values (?, ?)";
                        PreparedStatement pst = connection.prepareStatement(sql);
                        try {
                            pst.setLong(1, userId);
                            pst.setInt(2, goodsId);
                            pst.execute();
                        } catch (Exception e) {
                            throw e;
                        } finally {
                            pst.close();
                        }
                        return ResultBean.success("购买成功");
                    }
                } else {
                    return ResultBean.error("钱不够,你配吗");
                }
            }
        }
        return ResultBean.error("未找到商品");
    }

    public static ResultBean useGoods(Long userId, Integer goodsId) throws Exception {
        Connection connection = JDBCUtil.getConnection();
        String sql = "delete from user_goods_mapping " +
                "where id = (select id from user_goods_mapping ugm where user_id = ? and goods_id = ? limit 1)";
        PreparedStatement pst = connection.prepareStatement(sql);
        try {
            pst.setLong(1, userId);
            pst.setInt(2, goodsId);
            Integer result = pst.executeUpdate();
            if (result == 0) {
                return ResultBean.error("使用失败,你没有该物品");
            } else {
                return ResultBean.success("使用成功");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            pst.close();
        }
    }

    public static List<Goods> getHasGoodsIdList(Long userId) throws Exception {
        Connection connection = JDBCUtil.getConnection();
        String sql = "select goods_id from user_goods_mapping where user_id = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            pst.setLong(1, userId);
            rs = pst.executeQuery();
            List<Goods> result = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                result.add(Goods.goodsMap.get(rs.getInt(1)));
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

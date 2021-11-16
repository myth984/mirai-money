package org.example.mirai.plugin.enity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 商品
 */
public class Goods {
    private Integer id;
    private Integer type;
    private String name;
    private Integer price;
    private String describe;
    /**
     * 是否是消耗品
     */
    private boolean isConsumed = false;

    /**
     * 装备
     */
    public static final Integer TYPE_EQUIP = 0;
    /**
     * 耗材
     */
    public static final Integer TYPE_CONSUME = 1;

    public static List<Goods> allGoods = new ArrayList<>();
    public static HashMap<Integer, Goods> goodsMap = new HashMap();

    static {
        Goods g1 = new Goods(1, TYPE_EQUIP, "叶师傅的键盘", 100, "提高手速抢劫成功率+10%");
        Goods g2 = new Goods(2, TYPE_EQUIP, "余生转发的消息", 100, "被抢劫时,发送cos(cos机器人坏了),降低抢劫犯精力,反抗成功率+10%");
        Goods g3 = new Goods(3, TYPE_EQUIP, "panda的内裤", 0, "一股骚味,狗都不买,被抢劫时投诉panda(panda不让投诉了)反抗成功率-100%");
        Goods g4 = new Goods(4, TYPE_CONSUME, "小胶带", 100, "使用 [小胶带 @xxx] 禁言1分钟 可重复购买", true);
        Goods g5 = new Goods(5, TYPE_CONSUME, "大胶带", 500, "使用 [大胶带 @xxx] 禁言5分钟 可重复购买", true);
        Goods g6 = new Goods(6, TYPE_CONSUME, "万径人踪灭", 1000, "使用 [万径人踪灭] 全体禁言10分钟 可重复购买(小心叶师傅怒火)", true);
        allGoods.add(g1);
        allGoods.add(g2);
        allGoods.add(g3);
        allGoods.add(g4);
        allGoods.add(g5);
        allGoods.add(g6);
        allGoods.forEach(i -> {
            goodsMap.put(i.getId(), i);
        });

    }

    public Goods(Integer id, Integer type, String name, Integer price, String describe) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.price = price;
        this.describe = describe;
    }

    public Goods(Integer id, Integer type, String name, Integer price, String describe, boolean isConsumed) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.price = price;
        this.describe = describe;
        this.isConsumed = isConsumed;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Goods goods = (Goods) o;
        return id.equals(goods.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isConsumed() {
        return isConsumed;
    }

    public void setConsumed(boolean consumed) {
        isConsumed = consumed;
    }
}

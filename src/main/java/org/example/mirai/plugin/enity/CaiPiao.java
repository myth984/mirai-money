package org.example.mirai.plugin.enity;

import java.util.Date;

public class CaiPiao {
    Long userId;
    Integer money;
    Date date;
    // 0 代表不可用 1 代表可用
    Integer sure;
    Integer number;

    public static final int SURE_OK = 1;
    public static final int SURE_ERROR = 0;

    public Integer getSure() {
        return sure;
    }

    public void setSure(Integer sure) {
        this.sure = sure;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

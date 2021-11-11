package org.example.mirai.plugin.enity;

import java.util.Date;

public class SignLog {
    private Long UserId;
    private Date date;

    public Long getUserId() {
        return UserId;
    }

    public void setUserId(Long userId) {
        UserId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

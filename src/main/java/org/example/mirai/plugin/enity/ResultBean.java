package org.example.mirai.plugin.enity;

public class ResultBean {
    private Boolean success;
    private String msg;

    public ResultBean(Boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public ResultBean(String msg) {
        this.success = true;
        this.msg = msg;
    }

    public static ResultBean error(String msg) {
        return new ResultBean(false, msg);
    }

    public static ResultBean success(String msg) {
        return new ResultBean(true, msg);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

package com.xxbeam.DTO;

/**
 * 返回数据实体类
 */
public class ResultDTO{

    public static final String CODE_SUCCESS = "success";//成功
    public static final String CODE_ERROR = "error";//错误

    public static final String ACTION_RESPONSE = "response";//回复消息
    public static final String ACTION_POST = "post";//发送消息

    private String action = ACTION_RESPONSE;

    private Integer type;

    private String messageId;

    /**
     * 状态码 success/error
     */
    private String code;

    private String desc;

    private Object object;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}

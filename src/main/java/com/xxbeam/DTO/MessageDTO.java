package com.xxbeam.DTO;

import java.util.List;

/**
 * 接收数据实体类
 */
public class MessageDTO {

    private String messageId;

    /**
     * 类型 0--查询自身连接信息 1--发送消息
     */
    private Integer type;

    private List<String> uuids;

    private String message;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<String> getUuids() {
        return uuids;
    }

    public void setUuids(List<String> uuids) {
        this.uuids = uuids;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

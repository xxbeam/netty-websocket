package com.xxbeam.utils;

import com.xxbeam.DTO.MessageDTO;
import com.xxbeam.DTO.ResultDTO;

public class ResultUtil {

    public static ResultDTO responseOK(MessageDTO messageDTO){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setAction(ResultDTO.ACTION_RESPONSE);
        resultDTO.setCode(ResultDTO.CODE_SUCCESS);
        resultDTO.setMessageId(messageDTO.getMessageId());
        resultDTO.setType(messageDTO.getType());
        return resultDTO;
    }

    public  static ResultDTO responseOK(MessageDTO messageDTO,Object object){
        ResultDTO resultDTO = responseOK(messageDTO);
        resultDTO.setObject(object);
        return resultDTO;
    }

    public static ResultDTO responseError(MessageDTO messageDTO,String desc){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setAction(ResultDTO.ACTION_RESPONSE);
        resultDTO.setCode(ResultDTO.CODE_ERROR);
        resultDTO.setMessageId(messageDTO.getMessageId());
        resultDTO.setType(messageDTO.getType());
        resultDTO.setDesc(desc);
        return resultDTO;
    }

    public static ResultDTO exception(String message){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setAction(ResultDTO.ACTION_RESPONSE);
        resultDTO.setCode(ResultDTO.CODE_ERROR);
        resultDTO.setType(Consts.MESSAGE_TYPE_EXCEPTION);
        resultDTO.setDesc(message);
        return resultDTO;
    }

    public static ResultDTO post(MessageDTO messageDTO,Object object){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setAction(ResultDTO.ACTION_POST);
        resultDTO.setCode(ResultDTO.CODE_SUCCESS);
        resultDTO.setMessageId(messageDTO.getMessageId());
        resultDTO.setType(messageDTO.getType());
        resultDTO.setObject(object);
        return resultDTO;
    }

}

package com.xxbeam.handler;

import com.alibaba.fastjson.JSON;
import com.xxbeam.DTO.ChannelDTO;
import com.xxbeam.DTO.MessageDTO;
import com.xxbeam.DTO.ResultDTO;
import com.xxbeam.netty.WebSocketChannelGroup;
import com.xxbeam.utils.Consts;
import com.xxbeam.utils.ResultUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageHandler {

        public void handler(MessageDTO messageDTO, ChannelHandlerContext ctx){
            Integer type = messageDTO.getType();
            if(type!=null){
                if(type == Consts.MESSAGE_TYPE_CHANNELINFO){
                    //查询本连接的通道信息
                   this.getChannelInfo(messageDTO,ctx);
                   return;
                }else if(type == Consts.MESSAGE_TYPE_SENDMESSAGE){
                    //向指定通道发送消息
                    this.sendMessageByUuids(messageDTO,ctx);
                    return;
                }else{
                    ResultDTO resultDTO = ResultUtil.exception("type不正确");
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultDTO)));
                }
            }else{
                ResultDTO resultDTO = ResultUtil.exception("type为空");
                ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultDTO)));
            }
        }

    /**
     * 获得通道信息
     * @param messageDTO
     * @param ctx
     */
    private void getChannelInfo(MessageDTO messageDTO,ChannelHandlerContext ctx){
        Channel ch = ctx.channel();
        ChannelId chId = ch.id();
        String uuid = WebSocketChannelGroup.CHANNEL_UUID_MAP.get(chId);
        ChannelDTO channelDTO = new ChannelDTO();
        channelDTO.setUuid(uuid);

        ResultDTO resultDTO = ResultUtil.responseOK(messageDTO,channelDTO);
        ctx.channel().write(new TextWebSocketFrame(JSON.toJSONString(resultDTO)));
    }

    /**
     * 向指定通道发送消息
     * @param messageDTO
     */
    private void sendMessageByUuids(MessageDTO messageDTO,ChannelHandlerContext ctx){
        List<String> uuids = messageDTO.getUuids();
        ResultDTO resultDTO = ResultUtil.post(messageDTO,messageDTO.getMessage());
        resultDTO.setObject(messageDTO.getMessage());
        for (int i = 0; i < uuids.size(); i++) {
            ChannelId chid = WebSocketChannelGroup.UUID_CHANNEL_MAP.get(uuids.get(i));
            Channel channel = WebSocketChannelGroup.channels.find(chid);
            if(channel.isWritable()){
                channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultDTO)));
            }
        }

        //给发送发回复消息
        resultDTO = ResultUtil.responseOK(messageDTO);
        ctx.channel().write(new TextWebSocketFrame(JSON.toJSONString(resultDTO)));
    }

}

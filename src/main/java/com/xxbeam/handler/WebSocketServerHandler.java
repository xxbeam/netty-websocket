package com.xxbeam.handler;

import com.alibaba.fastjson.JSON;
import com.xxbeam.DTO.MessageDTO;
import com.xxbeam.DTO.ResultDTO;
import com.xxbeam.netty.WebSocketChannelGroup;
import com.xxbeam.utils.ResultUtil;
import com.xxbeam.utils.UuidUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private WebSocketServerHandshaker handshaker;

    @Autowired
    private MessageHandler messageHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        //第一次握手请求消息由http协议承载，所以他是一个http消息，执行handleHttpRequest方法处理握手请求
        //传统http接入
        if(msg instanceof FullHttpRequest){
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }

        //websocket接入
        if(msg instanceof WebSocketFrame){
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 处理http请求
     * @param ctx
     * @param req
     */
    private void handleHttpRequest(ChannelHandlerContext ctx,FullHttpRequest req) throws Exception{

        //如果http解码失败，则返回HTTP异常
        //如果消息头中没有Upgrade或者它的值不是websocket，则返回400
        if(!req.decoderResult().isSuccess()||(!"websocket".equals(req.headers().get("Upgrade")))){
            sendHttpResponse(ctx,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST));
        }

        //创建握手处理类之后，会将websocket相关的编码和解码类动态添加到ChannelPipeline中，服务端就可以自动对websocket消息进行解码，后面的业务handler可以直接对websocket对象进行操作
        String wsUrl = "ws://"+req.headers().get("Host")+"/ws";
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(wsUrl,null,false);
        handshaker = factory.newHandshaker(req);
        if(handshaker==null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else{
            handshaker.handshake(ctx.channel(),req);
        }
        this.addChannel(ctx.channel());
    }

    /**
     * 握手后维护通道
     * 为解决同一时间可能存在多个握手上来可能导致线程先后然后导致通道维护错误，将此方法改为同步
     * @param ch
     */
    private synchronized void addChannel(Channel ch){
        //握手后将通道加入组
        WebSocketChannelGroup.channels.add(ch);
        //生成唯一id
        String uuid = UuidUtil.getUuid();
        //维护本地uuid和通道ID的对应关系
        WebSocketChannelGroup.CHANNEL_UUID_MAP.put(ch.id(), uuid);
        WebSocketChannelGroup.UUID_CHANNEL_MAP.put(uuid,ch.id());
    }

    /**
     * 处理websocket数据
     * @param ctx
     * @param frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){
        //判断是否是关闭链路指令
        if(frame instanceof CloseWebSocketFrame){
            handshaker.close(ctx.channel(),((CloseWebSocketFrame) frame).retain());
            return;
        }
        //判断是否是ping消息
        if(frame instanceof PingWebSocketFrame){
            Channel ch = ctx.channel();
            ChannelId cid = ch.id();
            String uuid = WebSocketChannelGroup.CHANNEL_UUID_MAP.get(cid);
            if(StringUtils.isEmpty(uuid)){
                //不存在则关闭
                handshaker.close(ctx.channel(),((CloseWebSocketFrame) frame).retain());
            }else{
                ch.write(new PongWebSocketFrame(frame.content().retain()));
            }
            return;
        }
        //返回应答消息
        String request = null;
        try {
            if(frame instanceof TextWebSocketFrame){
                //文本
                request = ((TextWebSocketFrame) frame).text();
            }else if(frame instanceof ContinuationWebSocketFrame){
                //超大文本或二进制
                request = ((ContinuationWebSocketFrame) frame).text();
            }else{
                //二进制数据
                request = new String(frame.content().array(),"utf-8");
            }
            if(StringUtils.isNotEmpty(request)){
                MessageDTO messageDTO = JSON.parseObject(request, MessageDTO.class);
                messageHandler.handler(messageDTO,ctx);
            }else{
                ResultDTO resultDTO = ResultUtil.exception("消息为空");
                ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultDTO)));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            ResultDTO resultDTO = ResultUtil.exception(e.getMessage());
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultDTO)));
        }
    }

    /**
     * 发送http的返回
     * @param ctx
     * @param res
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx,FullHttpResponse res){

        //返回应答给客户端
        if(res.status().code()!=200){
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            res.headers().set(HttpHeaderNames.CONTENT_LENGTH, res.content().readableBytes());
        }

        //如果是非keep-Alive,关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if(!res.headers().get(HttpHeaderValues.KEEP_ALIVE).equals("keep-alive") || res.status().code() != 200){
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        logger.error(cause.getMessage());
        ResultDTO resultDTO = ResultUtil.exception(cause.toString());
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultDTO)));
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        Channel ch = ctx.channel();
        WebSocketChannelGroup.removeChannel(ch);
    }
}

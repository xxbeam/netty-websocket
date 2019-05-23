package com.xxbeam.netty;

import com.xxbeam.handler.WebSocketServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class WebSocketServer {

    private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    @Value("${websocket.port:18080}")
    private int port;

    @Autowired
    private WebSocketServerHandler webSocketServerHandler;

    @PostConstruct
    private void init(){

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();

        try {

            server.group(boss,work).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new HttpServerCodec());
                    pipeline.addLast(new HttpObjectAggregator(1024*1024));
//		pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH,null, true));
                    pipeline.addLast(new ChunkedWriteHandler());
//		pipeline.addLast(new WebSocketIndexPageHandler(WEBSOCKET_PATH));
                    pipeline.addLast(webSocketServerHandler);
                }
            });

            //清空连接池
            WebSocketChannelGroup.channels.clear();
            WebSocketChannelGroup.UUID_CHANNEL_MAP.clear();
            WebSocketChannelGroup.CHANNEL_UUID_MAP.clear();

            Channel ch = server.bind(port).sync().channel();
            logger.info("Websocket server start at port " + port + ".");
            logger.info("Open your browser and navigate to http://localhost:" + port);

            ch.closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }

    }

}

package com.xxbeam.task;

import com.xxbeam.netty.WebSocketChannelGroup;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * 定时判断各个通道的状态
 */
@Component
public class ChannelStatusTask {

    private static Logger logger = LoggerFactory.getLogger(ChannelStatusTask.class);

    /**
     * 每小时执行一次
     */
    @Scheduled(cron="0 0 * * * ?")
    private void process(){
        ChannelGroup channels =  WebSocketChannelGroup.channels;
        Iterator<Channel> iterator =  channels.iterator();
        while (iterator.hasNext()){
            Channel channel = iterator.next();
            if(!channel.isOpen()){
                logger.info(channel.id().toString()+" has closed, remove from group");
                WebSocketChannelGroup.removeChannel(channel);
            }
        }
    }

}

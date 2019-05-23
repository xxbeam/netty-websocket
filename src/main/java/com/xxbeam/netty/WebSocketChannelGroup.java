package com.xxbeam.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class WebSocketChannelGroup {

	//channel组
	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	//
    public static Map<ChannelId,String> CHANNEL_UUID_MAP = new HashMap<>();

    public static Map<String,ChannelId> UUID_CHANNEL_MAP = new HashMap<>();

    public static synchronized void removeChannel(Channel channel){
		ChannelId cid = channel.id();
		//解除该通道和注册号对应关系
		String uuid = WebSocketChannelGroup.CHANNEL_UUID_MAP.get(cid);
		if(StringUtils.isNotEmpty(uuid)){
			WebSocketChannelGroup.CHANNEL_UUID_MAP.remove(cid);
			WebSocketChannelGroup.UUID_CHANNEL_MAP.remove(uuid);
		}
		//channel组中移除该channel
		WebSocketChannelGroup.channels.remove(channel);
	}
}

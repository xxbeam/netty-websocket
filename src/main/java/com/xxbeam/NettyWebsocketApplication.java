package com.xxbeam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NettyWebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(NettyWebsocketApplication.class, args);
	}

}

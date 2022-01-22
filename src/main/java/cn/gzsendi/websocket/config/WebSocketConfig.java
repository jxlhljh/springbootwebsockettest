package cn.gzsendi.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import cn.gzsendi.websocket.handler.MyWebSocketHandler;
import cn.gzsendi.websocket.interceptor.WebSocketInterceptor;

/**
* @Description: websocket配置
* @Author: liujh
* @Date: 2021/12.25
*/
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{
	
	@Autowired
	MyWebSocketHandler myWebSocketHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //socket通道
        //指定处理器和路径
		registry.addHandler(myWebSocketHandler, "/websocket")
                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*");
	}

}

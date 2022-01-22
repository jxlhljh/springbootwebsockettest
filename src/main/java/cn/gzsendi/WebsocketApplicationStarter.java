package cn.gzsendi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import cn.gzsendi.websocket.service.RobotService;

//测试地址：http://ip:8081/remotewin?accessToken=123456
//或者 http://ip:8081/page/remotewin.html?accessToken=123456
//可以在虚机里面启动服务，然后在电脑本机进行测试
//注意：本程序要求服务器不能在本机, 不能在本地127.0.0.1测试，在本地电脑测试会产生镜中镜的效果，无限下去。
@SpringBootApplication
public class WebsocketApplicationStarter {

	public static void main(String[] args) {
		
		SpringApplicationBuilder builder = new SpringApplicationBuilder(WebsocketApplicationStarter.class);
		ConfigurableApplicationContext ctx = builder.headless(false).run(args);
        
		//服务端开启定时抓取截图并发给客户端的处理
		RobotService robotService = ctx.getBean(RobotService.class);
		robotService.startCaputureTask();
        
        
	}

}

package cn.gzsendi.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

//测试地址：http://192.168.56.1:8081/remotewin?accessToken=123456
//或者 http://192.168.56.1:8081/page/remotewin.html?accessToken=123456
//可以在虚机里面启动服务，然后在电脑本机进行测试
//注意：本程序要求服务器不能在本机, 不能在本地127.0.0.1测试，在本地电脑测试会产生镜中镜的效果，无限下去。
@Controller
public class RouterController {
	
    @RequestMapping("/remotewin")
    public String remotewin(){
        return "remotewin";
    }
}

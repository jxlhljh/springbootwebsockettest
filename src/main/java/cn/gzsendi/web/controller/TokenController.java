package cn.gzsendi.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tokenController")
public class TokenController {
	
	@Value("${accessToken:123456}")
    private String accessToken;
	
	@GetMapping("/check")
	public String check(String accessToken){
		return this.accessToken.equals(accessToken) ? "success" :"fail";
	}

}

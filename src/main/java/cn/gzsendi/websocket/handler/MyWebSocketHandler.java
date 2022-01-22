package cn.gzsendi.websocket.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import cn.gzsendi.framework.utils.JsonUtil;
import cn.gzsendi.websocket.constant.ConstantPool;
import cn.gzsendi.websocket.service.RobotService;

/**
* @Description: WebSocket处理器
* @Author: liujh
* @Date: 2021/12.25
*/
@Component
public class MyWebSocketHandler implements WebSocketHandler{
	
	private Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);
	public static Map<String,WebSocketSession> webSocketSessions = new ConcurrentHashMap<String, WebSocketSession>();
	
	@Autowired RobotService robotService;

    /**
     * @Description: 用户连接上WebSocket的回调
     * @Param: [webSocketSession]
     * @return: void
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        
    	logger.info("用户:{},连接WebSSH", webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY));
    	webSocketSessions.put(webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY).toString(), webSocketSession);
        
    }
    
    /**
     * @Description: 收到消息的回调
     * @Param: [webSocketSession, webSocketMessage]
     * @return: void
     */
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
    	
    	//设置更新最后一后键盘或鼠标事件的到达时间
    	robotService.setLastestActionTime(System.currentTimeMillis());
        
    	if (webSocketMessage instanceof TextMessage) {
    		
    		//logger.info("用户:{},发送命令:{}", webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY), webSocketMessage.toString());
    		Map<String,Object> playload = JsonUtil.castToObject(webSocketMessage.getPayload().toString());
    		
    		//回放处理客户端发送过来的键盘或鼠标事件,在服务端这边重新执行一遍
    		robotService.actionEvent(playload);
    		
    	} else if (webSocketMessage instanceof BinaryMessage) {
        	
        	

        } else if (webSocketMessage instanceof PongMessage) {
        	
        	

        } else {
            logger.error("Unexpected WebSocket message type: " + webSocketMessage);
        }
    }

    /**
     * @Description: 出现错误的回调
     * @Param: [webSocketSession, throwable]
     * @return: void
     */
    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        logger.error("数据传输错误");
    }

    /**
     * @Description: 连接关闭的回调
     * @Param: [webSocketSession, closeStatus]
     * @return: void
     */
    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        logger.info("用户:{}断开webssh连接", String.valueOf(webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY)));
        webSocketSessions.remove(webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY).toString());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

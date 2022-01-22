package cn.gzsendi.websocket.service;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import cn.gzsendi.framework.utils.JsonUtil;
import cn.gzsendi.websocket.handler.MyWebSocketHandler;

/**
 * 定时抓取截图以及处理服务端的事件回放（键盘与鼠标）
 * @author liujh
 *
 */
@Service
public class RobotService {
	
	private Logger logger = LoggerFactory.getLogger(RobotService.class);
	private Long lastestActionTime = System.currentTimeMillis(); //记录最后一后键盘或鼠标事件的到达时间
	private int remoteImageWidth ; //远程服务端的屏幕宽
	private int remoteImageHeigth ; //远程服务端的屏幕高
	private Robot robot = null;
	private Rectangle rectangle = null;
	
	public RobotService() {
		
		try {
			robot = new Robot();//核心机器人类，用于截图，键盘或鼠标事件的重放执行。
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension dimension = toolkit.getScreenSize();//获取到远程桌面的屏幕大小信息
			rectangle = new Rectangle(0, 0, (int)dimension.getWidth(), (int)dimension.getHeight());
		} catch (AWTException e) {
			logger.error("",e);
		}
		
	}
	
	/**
	 * 进行截图任务的处理，如果有客户端连接上来，将进行截图并广播发送给所有的客户端
	 */
	public void startCaputureTask(){
		
		while(true){
		
			try {
				
				//100毫秒检查一次，如果有客户端，并且满足需要截图的条件，就截图一张发给所有的客户端，可以调整这个值，值越小延迟越小
				Thread.sleep(100l);
				
				//遍历所有在线的客户端
				Map<String,WebSocketSession> webSocketSessions  = MyWebSocketHandler.webSocketSessions;
				
				//没有websocket客户端连上的话，直接就退出本轮循环，不需要进行截图处理
				if(webSocketSessions.size() == 0 ) {
					//logger.info("webSocketSessions.size() == 0");
					continue;
				}
				
				//如果超过5秒没有收到键盘或鼠标事件，说明可以停止截图给客户端，节省性能。
				if((System.currentTimeMillis() - lastestActionTime) > 5000){
					//logger.info("exceed 5 seconds not keyboard event arrived, stop send images.");
					continue;
				}
				
				byte[] data = getCapture(robot,rectangle);
				ImageIcon icon = new ImageIcon(data);
				remoteImageWidth = icon.getIconWidth();
				remoteImageHeigth = icon.getIconHeight();
				
				//遍历发送给所有的客户端连接
				for(WebSocketSession webSocketSession : webSocketSessions.values()) {
					if(webSocketSession.isOpen()) {
						webSocketSession.sendMessage(new BinaryMessage(data));
					}
				}
			
			} catch (Exception e) {
				logger.error("startCaputureTaskError",e);
			}
		
		}
		
	}
	
	//回放处理客户端发送过来的键盘或鼠标事件
	public void actionEvent(Map<String,Object> playload){
		
		String openType = JsonUtil.getString(playload, "openType");
		
		if("mousedown".equals(openType)){
			
			//鼠标按下事件
			logger.info("鼠标按下事件,{}",JsonUtil.toJSONString(playload));
			
			int clientX = JsonUtil.getInteger(playload, "clientX");
    		int clientY = JsonUtil.getInteger(playload, "clientY");
    		int button = JsonUtil.getInteger(playload, "button");
    		int imageWidth = JsonUtil.getInteger(playload, "imageWidth");
    		int imageHeight = JsonUtil.getInteger(playload, "imageHeight");
    		
    		//这里为什么要这样转？说明如下：
    		//假如浏览器的image区域为1200*800,远程桌面的截图区为900*700
    		//那么在浏览器上点击了clientX=77,clientY=88这个坐标时，实际上在远程
    		//桌面上正确的坐标应该为：
    		//remoteClientX = clientX * remoteImageWidth/imageWidth;
    		//即：remoteClientX = 77 * 900 / 1200
    		//remoteClientY同理.
    		int remoteClientX = clientX * remoteImageWidth/imageWidth;
    		int remoteClientY = clientY * remoteImageHeigth/imageHeight;
    		
    		//移动鼠标到正确的坐标
    		robot.mouseMove( remoteClientX , remoteClientY );
    		
    		//然后进行鼠标的按下
    		if(button == 0) {
    			robot.mousePress(InputEvent.BUTTON1_MASK);//左键
    		}else if(button == 1) {
    			robot.mousePress(InputEvent.BUTTON2_MASK);//中间键
    		}else if(button == 2) {
    			robot.mousePress(InputEvent.BUTTON3_MASK);//右键
    		}
    		
			
		}else if("mouseup".equals(openType)){
			
			//鼠标弹开事件
			logger.info("鼠标弹开事件,{}",JsonUtil.toJSONString(playload));
			
			int clientX = JsonUtil.getInteger(playload, "clientX");
    		int clientY = JsonUtil.getInteger(playload, "clientY");
    		int button = JsonUtil.getInteger(playload, "button");
    		int imageWidth = JsonUtil.getInteger(playload, "imageWidth");
    		int imageHeight = JsonUtil.getInteger(playload, "imageHeight");
    		int remoteClientX = clientX*remoteImageWidth/imageWidth;
    		int remoteClientY = clientY*remoteImageHeigth/imageHeight;
    		
    		//移动鼠标到正确的坐标
    		robot.mouseMove( remoteClientX , remoteClientY );
			
    		//然后进行鼠标的弹起
    		if(button == 0) {
    			robot.mouseRelease(InputEvent.BUTTON1_MASK);//左键
    		}else if(button == 1) {
    			robot.mouseRelease(InputEvent.BUTTON2_MASK);//中间键
    		}else if(button == 2) {
    			robot.mouseRelease(InputEvent.BUTTON3_MASK);//右键
    		}
    		
			
		}else if("mousemove".equals(openType)){
			
			//鼠标移动事件
			
			int clientX = JsonUtil.getInteger(playload, "pageX");
    		int clientY = JsonUtil.getInteger(playload, "pageY");
    		int imageWidth = JsonUtil.getInteger(playload, "imageWidth");
    		int imageHeight = JsonUtil.getInteger(playload, "imageHeight");
    		int remoteClientX = clientX*remoteImageWidth/imageWidth;
    		int remoteClientY = clientY*remoteImageHeigth/imageHeight;
    		
    		//将鼠标进行移动
    		robot.mouseMove( remoteClientX , remoteClientY );
    		
		}else if("keydown".equals(openType)){
			
			//键盘按下事件
			logger.info("键盘按下事件,{}",JsonUtil.toJSONString(playload));
			
			int keyCode = JsonUtil.getInteger(playload, "keyCode");
			robot.keyPress(changeKeyCode(keyCode));
			
		}else if("keyup".equals(openType)){
			
			//键盘弹开事件
			logger.info("键盘弹开事件,{}",JsonUtil.toJSONString(playload));
			int keyCode = JsonUtil.getInteger(playload, "keyCode");
			robot.keyRelease(changeKeyCode(keyCode));
		}
		
	}
	
	//进行keyCode的改变，因为浏览器的键盘事件和Java的awt的事件代码，有些是不一样的，需要进行转换，
	//比如浏览器中13表示回车，但在Java的awt中是用10表示
	//这里可能转换不全，比如F1-F12键都没有处理，因为浏览器现在没有禁用这些键，如果需要支持，可以继续在这里加上
	private int changeKeyCode(int sourceKeyCode){
		
		//回车
		if(sourceKeyCode == 13) return 10;
		
		//,< 188 -> 44
		if(sourceKeyCode == 188) return 44;
		
		//.>在Js中为190，但在Java中为46
		if(sourceKeyCode == 190) return 46;
		
		// /?在Js中为191，但在Java中为47
		if(sourceKeyCode == 191) return 47;
		
		//;: 186 -> 59
		if(sourceKeyCode == 186) return 59;
		
		//[{ 219 -> 91
		if(sourceKeyCode == 219) return 91;
		
		//\| 220 -> 92
		if(sourceKeyCode == 220) return 92;
		
		//-_ 189->45
		if(sourceKeyCode == 189) return 45;
		
		//=+ 187->61
		if(sourceKeyCode == 187) return 61;
		
		//]} 221 -> 93
		if(sourceKeyCode == 221) return 93;
		
		//DEL
		if(sourceKeyCode == 46) return 127;
		
		//Ins
		if(sourceKeyCode == 45) return 155;
		
		return sourceKeyCode;
	}
	
	/**
	 * 得到屏幕截图数据
	 * @return
	 */
	private byte[] getCapture(Robot robot,Rectangle rectangle) {
		
		BufferedImage bufferedImage =  robot.createScreenCapture(rectangle);
		
		//获得一个内存输出流
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		//将图片数据写入内存流中
		try {
			
			//原始图片，现在用下面的压缩图片法替换了
			ImageIO.write(bufferedImage, "jpg", baos);
			
			//进行图片压缩，图片尺寸不变，压缩图片文件大小outputQuality实现,参数1为最高质量
			//Thumbnails.of(bufferedImage).scale(1f).outputQuality(0.25f).outputFormat("jpg").toOutputStream(baos);
			
		} catch (IOException e) {
			logger.error("图片写入出现异常",e);
		}
		
		return baos.toByteArray();
	}
	
	public int getRemoteImageWidth() {
		return remoteImageWidth;
	}

	public void setRemoteImageWidth(int remoteImageWidth) {
		this.remoteImageWidth = remoteImageWidth;
	}

	public int getRemoteImageHeigth() {
		return remoteImageHeigth;
	}

	public void setRemoteImageHeigth(int remoteImageHeigth) {
		this.remoteImageHeigth = remoteImageHeigth;
	}
	
	public Long getLastestActionTime() {
		return lastestActionTime;
	}

	public void setLastestActionTime(Long lastestActionTime) {
		this.lastestActionTime = lastestActionTime;
	}
	
}

package com.wang.gongzuoliu.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

/**
 * WebSocket
 *
 * @Description TODO
 * @Author dongjianping
 * @Date 2019/11/25 15:32
 */

@ServerEndpoint("/websocket/{uid}")
@Component
public class WebSocketServer {

    // 获取日志记录器Logger，名字为本类类名
    private static Logger log = Logger.getLogger("WebSocketServer");
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //接收uid
    private String uid = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid) {
        this.session = session;
        // 加入set中
        webSocketSet.add(this);
        // 在线数加1
        addOnlineCount();

        log.info("有新窗口开始监听:" + uid + ",当前在线人数为" + getOnlineCount());

        this.uid = uid;

//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("message", "连接成功!");
//            sendInfo(jsonObject.toString(), uid);
//        } catch (IOException e) {
//            log.info("websocket IO异常");
//        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        // 从set中删除
        webSocketSet.remove(this);
        // 在线数减1
        subOnlineCount();
        log.info("有一个连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来自" + uid + "的消息:" + message);
        // 发消息
        for (WebSocketServer webSocketServer : webSocketSet) {
            try {
                webSocketServer.sendInfo(message, uid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("发生错误");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message, @PathParam("uid") String uid) throws IOException {
        log.info("推送消息到窗口" + uid + "，推送内容:" + message);
        for (WebSocketServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个uid的，为null则全部推送
                if (uid == null) {
                    item.sendMessage(message);
                } else if (item.uid.equals(uid)) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}

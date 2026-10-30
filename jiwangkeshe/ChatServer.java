//实现了服务器监听客户端连接、为每个客户端分配独立线程处理消息、并通过图形界面展示服务器状态
package jiwangkeshe;
import java.io.IOException;
import java.net.*;
import java.util.*;

//服务器启动
public class ChatServer {
 static HashMap<String, Socket> userSocketMap = new HashMap<>();
 public ServerFrame serverFrame;
 public ChatServer() 
 {
    try{
       //建立服务器Socket监听 
       ServerSocket serverSocket = new ServerSocket(Port.SERVER_PORT);    //端口号：8888
       //打开服务器界面
       serverFrame = new ServerFrame();
       ServerInfoBean serverInfo = getServerIP();
       loadServerInfo(serverInfo);
       //解决多客户端连接的循环
       while(true){
          //等待连接，阻塞实现，会得到一个客户端连接
          Socket socket = serverSocket.accept();
          //启动一个线程来一直接收消息
          ServerHandler serverHandler = new ServerHandler(socket, serverFrame);
          serverHandler.start();
          
          System.out.println("CONNECTED：" + socket);
       }
       
    }catch (IOException e) {
       e.printStackTrace();
    }
    
 }
 
  //获取服务器的主机名和IP地址,返回服务器IP等信息
 public ServerInfoBean getServerIP() {
    ServerInfoBean sib = null;
    try {
       InetAddress serverAddress = InetAddress.getLocalHost();
       byte[] ipAddress = serverAddress.getAddress();
       sib = new ServerInfoBean();
       sib.setIp(serverAddress.getHostAddress());
       sib.setHostName(serverAddress.getHostName());
       sib.setPort(Port.SERVER_PORT);

       System.out.println("Server IP is:" + (ipAddress[0] & 0xff) + "."
             + (ipAddress[1] & 0xff) + "." + (ipAddress[2] & 0xff) + "."
             + (ipAddress[3] & 0xff));
    } catch (Exception e) {
       System.out.println("CouLd not get Server IP." + e);
    }
    return sib;
 }
 public void loadServerInfo(ServerInfoBean serverInfo) {
    serverFrame.serverInfoPanel.txtIP.setText(serverInfo.getIp());
    serverFrame.serverInfoPanel.txtServerName.setText(serverInfo.getHostName());
   // serverFrame.serverInfoPanel.txtLog.setText("SERVER SUCCESS");
    
 }

 //主函数
 public static void main(String[] args) {
    new ChatServer();
 }
}

package jiwangkeshe;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.swing.*;

//服务器启动一个线程，一直读消息
public class ServerHandler extends Thread {
	
 Socket socket;	
 ServerFrame serverFrame;

 public ServerHandler(Socket socket, ServerFrame serverFrame) {
    this.socket = socket;
    this.serverFrame = serverFrame;
 }
 
 static ArrayList<String> onlineUsers = new ArrayList<>();
 static ArrayList<Socket> onlineSockets = new ArrayList<>();

 // 用户账号文件，相对于启动目录，每行格式为 用户名@密码
 static final String USER_FILE = "users.txt";

 @Override
 public void run() {

    // 默认重复拿
    while (true) {
       // 模拟一直拿消息
       Object object = IOStream.readMessage(socket);		//从socket当中读取消息
       System.out.println("Server receive：" + object);

     
          TransferInfo transferInfo = (TransferInfo) object;	//将对象强整形
          if (transferInfo.getStatusEnum() == InfoType.REGISTER) {
        	  registerHandler(transferInfo);
          }
          else if (transferInfo.getStatusEnum() == InfoType.LOGIN) {
             //登录消息类型
             loginHandler(transferInfo);
          }else if (transferInfo.getStatusEnum() == InfoType.CHAT) {
             //聊天信息类型
             chatHandler(transferInfo);
          }else if (transferInfo.getStatusEnum() == InfoType.EXIT) {
             //退出聊天类型
             logout(transferInfo);
             try {
                Thread.sleep(1000);
                socket.close();    //关闭socket连接
             } catch (InterruptedException e) {
               
                e.printStackTrace();
             } catch (IOException e) {
              
                e.printStackTrace();
             }
             this.interrupt();//关闭当前线程
             break;
          }
      // }
    }
 }
 
 //客户端登录处理
 public void loginHandler(TransferInfo transferInfo) {
	 String username1 = transferInfo.getUsername();
	    // --------- 新增：重复登录检测 ---------
	    if (onlineUsers.contains(username1)) {
	        // 构造一个登录失败的响应
	        transferInfo.setLoginSuccess(false);
	        transferInfo.setNotice("登录失败：用户 '" + username1 + "' 已在线");
	        transferInfo.setStatusEnum(InfoType.LOGIN);
	        IOStream.writeMessage(socket, transferInfo);
	        // 关闭当前连接
	        try { socket.close(); } catch (IOException ignored) {}
	        return;
	    }
    boolean flag = checkUserLogin(transferInfo);
    transferInfo.setLoginSuccess(false);
    if (flag) {
       //返回登录成功给客户端（登录消息）
       transferInfo.setLoginSuccess(true);
       transferInfo.setStatusEnum(InfoType.LOGIN);
       IOStream.writeMessage(socket, transferInfo);
       String username = transferInfo.getUsername();
       
       //统计在线人数
       onlineUsers.add(username);
       onlineSockets.add(socket);
       ChatServer.userSocketMap.put(username, socket);
       
       //返回用户上线信息给所有用户（系统消息）
       transferInfo = new TransferInfo();
       transferInfo.setStatusEnum(InfoType.NOTICE);
       String notice = ">>  ❤ Welcome " + username + " ❤";
       transferInfo.setNotice(notice);
       sendAll(transferInfo); 
       
       //发送最新的用户列表给客户端
       transferInfo = new TransferInfo();
       transferInfo.setUserOnlineArray(onlineUsers.toArray(new String[onlineUsers.size()]));
       transferInfo.setStatusEnum(InfoType.ULIST);
       sendAll(transferInfo);
       
  
       
     //发送信息给服务器日志
       log(notice);
    }else{
       //返回登录失败给客户端
       IOStream.writeMessage(socket, transferInfo);
       log(transferInfo.getUsername() + "LOGIN FAILED!");
    }
 }

 public void log(String log) {
	    System.out.println(log);
	}
 // ================ 新增注册处理方法 ================
 private void registerHandler(TransferInfo transferInfo) {
     String username = transferInfo.getUsername();
     String password = transferInfo.getPassword();
     boolean isRegistered = checkUserExists(username); // 检查用户名是否存在
     
     try {
         if (!isRegistered) {
             // 保存用户信息到文件
             saveUserToFile(username, password);
             transferInfo.setRegisterSuccess(true);
             transferInfo.setNotice("注册成功");
             log(username + " 注册成功");
         } else {
             transferInfo.setRegisterSuccess(false);
             transferInfo.setNotice("用户名已存在");
             log(username + " 注册失败：用户名重复");
         }
         
         // 返回注册结果给客户端
         IOStream.writeMessage(socket, transferInfo);
     } catch (IOException e) {
         transferInfo.setRegisterSuccess(false);
         transferInfo.setNotice("服务器内部错误");
         IOStream.writeMessage(socket, transferInfo);
         e.printStackTrace();
     }
 }
//================ 新增用户存在性检查 ================
 private boolean checkUserExists(String username) {
     try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(USER_FILE))) {
         String row;
         while ((row = dataInputStream.readLine()) != null) {
             String[] parts = row.split("@"); // 假设文件格式为 username@password
             if (parts[0].equals(username)) {
                 return true;
             }
         }
     } catch (IOException e) {
         e.printStackTrace();
     }
     return false;
 }
 // ================ 新增保存用户到文件 ================
		    private void saveUserToFile(String username, String password) throws IOException {
		        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(USER_FILE, true))) {
		            String userRecord = username + "@" + password + "\n";
		            dataOutputStream.writeBytes(userRecord);
		        }
		    }

  //广播给所有人
 public void sendAll(TransferInfo transferInfo) {
    for (int i = 0; i < onlineSockets.size(); i++){
       Socket tempSocket = onlineSockets.get(i);
       IOStream.writeMessage(tempSocket, transferInfo);
    }
 }

  //私聊
 public void send(TransferInfo transferInfo) {

 }

 public void chatHandler(TransferInfo transferInfo) {
	    if (transferInfo.isPrivate()) {
	        // 精确匹配接收者用户名（注意大小写敏感问题）
	        Socket targetSocket = ChatServer.userSocketMap.get(transferInfo.getReceiver().trim());
	        if (targetSocket != null && !targetSocket.isClosed()) {
	            IOStream.writeMessage(targetSocket, transferInfo);
				// 可选：给发送者回显消息
				IOStream.writeMessage(socket, transferInfo);
	        }
	    } else {
	        // 群发时排除发送者
	        for (Socket userSocket : onlineSockets) {
	            if (!userSocket.isClosed()) {
	                IOStream.writeMessage(userSocket, transferInfo);
	            }
	        }
	    }
	}
 
 //验证登录
 public boolean checkUserLogin(TransferInfo transferInfo) {
    try {
       String username = transferInfo.getUsername();
       String password = transferInfo.getPassword();
       FileInputStream fileInputStream = new FileInputStream(new File(USER_FILE));
       DataInputStream dataInputStream = new DataInputStream(fileInputStream);
       String row = null;
       while ((row = dataInputStream.readLine()) != null) {
          //从文件中读取的行
          String fileUser = row;
          if ((username + "@" + password).equals(row)) {
             System.out.println("Name and Password are correct!");
             return true;
          }else{
             System.out.println("Login failed!");
          }
       }
    } catch (FileNotFoundException e) {    
       e.printStackTrace();
    } catch (IOException e) {
       e.printStackTrace();
    }
 
    return false;
 }
 //用户退出处理，清理在线人数，刷新用户列表，告诉所有人，你已经离开

 private void logout(TransferInfo transferInfo) {
    String userName = transferInfo.getUsername();
    //将该用户从用户集合移除
    Iterator<String> userIter = onlineUsers.iterator();
    while(userIter.hasNext()) {
       if(userIter.next().equals(userName)) {
          userIter.remove();
       }
    }
    
    //将该用户从socket集合移除
    Iterator<Socket> socketIter = onlineSockets.iterator();
    while(socketIter.hasNext()) {
       Socket next = socketIter.next();
       if(socket == next) {
          socketIter.remove();
       }
    }
    
    //将user与Socket的关系从Map中移除
    ChatServer.userSocketMap.remove(userName);
    
    //给所有在线的用户发送下线消息
    String userName1 = transferInfo.getUsername();
    String notice = ">>用户 " + userName1 + " 已下线";
    transferInfo.setNotice(notice);
    transferInfo.setStatusEnum(InfoType.NOTICE);
    sendAll(transferInfo);
    
    //告诉其他人刷新用户列表
    transferInfo.setUserOnlineArray(onlineUsers.toArray(new String [onlineUsers.size()]));
    transferInfo.setStatusEnum(InfoType.ULIST);
    sendAll(transferInfo);
 }

}
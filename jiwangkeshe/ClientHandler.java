package jiwangkeshe;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

//每个客户端登录后启动一个独立线程，持续监听服务器发来的消息
public class ClientHandler extends Thread 
{
    private final Socket socket;//和服务器通信的 TCP 连接
    private final LoginFrame loginFrame;//登录界面的引用，用于在登录阶段出错时弹窗提示或关闭登录窗口
    private ChatFrame chatFrame;//聊天主界面，登录成功后由它来显示消息和用户列表
    private final Queue<TransferInfo> pendingMessages = new ConcurrentLinkedQueue<>();
    //在 chatFrame 尚未初始化前，临时缓存接收到的消息，等 UI 准备好再统一处理。
    private String username;//客户端当前用户的用户名
    public ClientHandler(Socket socket, LoginFrame loginFrame) //初始化
    {
        this.socket = socket;
        this.loginFrame = loginFrame;
    }
//主循环 (run 方法)
    @Override
    public void run() {
        try {
            while (!socket.isClosed()) //保持长连接，一直监听服务器发来的对象
            {
                Object object = IOStream.readMessage(socket);//阻塞读取一个对象 
                //若返回 null，表示服务器主动断开连接，调用 handleServerDisconnect()，并跳出循环
                if (object == null) 
                {
                    handleServerDisconnect();
                    break;
                }
                if (object instanceof TransferInfo) 
                {
                    TransferInfo transferInfo = (TransferInfo) object;
                    processTransferInfo(transferInfo);
                }
            }
        } finally {
            cleanupResources();
            //一旦 run 退出（异常或断开），调用 cleanupResources() 释放资源
        }
    }
//消息类型分发
//依据 TransferInfo.statusEnum（由服务器端设置的 InfoType 枚举值）决定走哪条逻辑分支。
    private void processTransferInfo(TransferInfo transferInfo) 
    {
        switch (transferInfo.getStatusEnum()) {
            case LOGIN:
                handleLoginResult(transferInfo);
                break;
            case CHAT:
                handleChatMessage(transferInfo);
                break;
            case NOTICE:
                handleSystemNotice(transferInfo);
                break;
            case ULIST:
                updateUserList(transferInfo);
                break;
            default:
                System.err.println("未知消息类型: " + transferInfo.getStatusEnum());
        }
    }
//登录结果处理
    private void handleLoginResult(TransferInfo transferInfo) 
    {
        if (transferInfo.getLoginSuccess()) //登录成功
        {
            this.username = transferInfo.getUsername();
            SwingUtilities.invokeLater(() -> {
                chatFrame = new ChatFrame(username, socket, this);
                loginFrame.dispose();
                processCachedMessages();
            });
        } else {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(loginFrame, 
                    "登录失败: " + transferInfo.getNotice(),
                    "错误", JOptionPane.ERROR_MESSAGE));
        }
    }
//聊天消息处理
    private void handleChatMessage(TransferInfo transferInfo) 
    {
        // 统一构造消息内容
        String prefix = transferInfo.isPrivate() ? "[私]" : "[群]";
        String fullMessage;
        if (transferInfo.isPrivate()) {
            fullMessage = prefix 
                    + transferInfo.getSender() 
                    + "->" 
                    + transferInfo.getReceiver() 
                    + ": " 
                    + transferInfo.getContent();
        } else {
            fullMessage = prefix 
                    + transferInfo.getSender() 
                    + ": " 
                    + transferInfo.getContent();
        }
        if (chatFrame == null) {
            pendingMessages.add(transferInfo);
            return;
        }
     // 直接拼接含表情的消息内容
        //String fullMessage = prefix + transferInfo.getContent();
        // 直接使用已构造的message变量 ✅
        SwingUtilities.invokeLater(() -> {
            if (chatFrame != null) {
                chatFrame.safeAppendMessage(fullMessage);
            }
        });
    }
//系统通知处理
    private void handleSystemNotice(TransferInfo transferInfo) {
        if (chatFrame == null) {
            pendingMessages.add(transferInfo);
            return;
        }
        
        SwingUtilities.invokeLater(() -> 
            chatFrame.safeAppendMessage(transferInfo.getNotice()));
    }
//在线列表更新
    private void updateUserList(TransferInfo transferInfo) {
        if (chatFrame == null) {
            pendingMessages.add(transferInfo);
            return;
        }
        
        SwingUtilities.invokeLater(() -> 
        chatFrame.updateUserList(
            Arrays.asList(transferInfo.getUserOnlineArray()) // 这里转换类型
        ));}
//缓存消息处理
    private void processCachedMessages() {
        while (!pendingMessages.isEmpty()) {
            TransferInfo msg = pendingMessages.poll();
            processTransferInfo(msg);
        }
    }
//断线处理
    public void onChatFrameInitialized(ChatFrame chatFrame) {
        this.chatFrame = chatFrame;
        processCachedMessages();
    }

    private void handleServerDisconnect() {
        SwingUtilities.invokeLater(() -> {
            if (chatFrame != null) {
                chatFrame.safeAppendMessage("\n[系统] 服务器连接已断开");
            }
            cleanupResources();
        });
    }

    private synchronized void cleanupResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭socket时出错: " + e.getMessage());
        }
        pendingMessages.clear();
    }

    // 供ChatFrame调用的清理方法
    public void cleanup() {
        cleanupResources();
        if (chatFrame != null) {
            chatFrame.dispose();
        }
    }
}
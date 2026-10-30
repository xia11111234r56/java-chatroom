package jiwangkeshe;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import javax.swing.*;

public class ChatFrame extends JFrame {
    private static final long serialVersionUID = 1709267049032639256L;

    private ClientHandler clientHandler;  // 后台通信线程
    private String username;              // 当前用户名
    private Socket socket;                // 与服务器的连接

    private JTextPane acceptPane;         // 聊天显示区域
    private JList<String> lstUser;        // 在线用户列表（只显示用户名）
    private JTextPane sendPane;           // 信息输入区域
    private JComboBox<String> reciverBox; // 收信人下拉框

    public ChatFrame(String username, Socket socket, ClientHandler clientHandler) {
        this.username = username;
        this.socket = socket;
        this.clientHandler = clientHandler;

        initUI();                       // 初始化界面
        setupWindowClosing();           // 配置关闭事件
        notifyInitializationComplete(); // 通知 Handler 可以推送缓存消息
    }

    private void initUI() {
        setTitle("Chat Room - " + username);
        setSize(800, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(5,5));

        // 聊天显示区
        acceptPane = new JTextPane();
        acceptPane.setEditable(false);
        JScrollPane messageScroll = new JScrollPane(acceptPane);
        add(messageScroll, BorderLayout.CENTER);

        // 在线用户列表（只显示用户名）
        DefaultListModel<String> userModel = new DefaultListModel<>();
        lstUser = new JList<>(userModel);
        lstUser.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        lstUser.setFixedCellWidth(150);
        JScrollPane userScroll = new JScrollPane(lstUser);
        userScroll.setPreferredSize(new Dimension(160, 0));
        add(userScroll, BorderLayout.WEST);

        // 发送区面板
        JPanel sendPanel = new JPanel(new BorderLayout(5,5));
        sendPane = new JTextPane();
        JScrollPane sendScroll = new JScrollPane(sendPane);
        sendScroll.setPreferredSize(new Dimension(0, 100));
        sendPanel.add(sendScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        reciverBox = new JComboBox<>();
        reciverBox.addItem("ALL");
        bottom.add(new JLabel("Send To:"));
        bottom.add(reciverBox);

        JButton sendBtn = new JButton("SEND");
        sendBtn.addActionListener(e -> handleSendMessage());
        bottom.add(sendBtn);

        sendPanel.add(bottom, BorderLayout.SOUTH);
        add(sendPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void handleSendMessage() {
        String content = sendPane.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "消息不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TransferInfo info = new TransferInfo();
		info.setSender(username);
		info.setContent(content);
		String receiver = (String) reciverBox.getSelectedItem();
		info.setPrivate(!"ALL".equals(receiver));
		info.setReceiver(receiver);
		info.setStatusEnum(InfoType.CHAT);

		IOStream.writeMessage(socket, info);
		sendPane.setText("");
    }

    private void setupWindowClosing() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    TransferInfo exitInfo = new TransferInfo();
                    exitInfo.setStatusEnum(InfoType.EXIT);
                    exitInfo.setUsername(username);
                    IOStream.writeMessage(socket, exitInfo);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        // 忽略
                    }
                    clientHandler.cleanup();
                }
            }
        });
    }

    private void notifyInitializationComplete() {
        SwingUtilities.invokeLater(() -> {
            clientHandler.onChatFrameInitialized(this);
        });
    }

    /**
     * 安全地追加接收到的消息到聊天区域
     */
    public void safeAppendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String text = acceptPane.getText();
            acceptPane.setText(text + (text.isEmpty() ? "" : "\n") + message);
            acceptPane.setCaretPosition(acceptPane.getDocument().getLength());
        });
    }

    /**
     * 更新在线用户列表和收件人下拉框
     */
    public void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            // 更新下拉框
            reciverBox.removeAllItems();
            reciverBox.addItem("ALL");
            users.forEach(reciverBox::addItem);

            // 更新列表
            DefaultListModel<String> model = (DefaultListModel<String>) lstUser.getModel();
            model.clear();
            users.forEach(model::addElement);
        });
    }
}

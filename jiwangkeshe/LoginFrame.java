//登录界面类
package jiwangkeshe;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private static final Integer FRAME_WIDTH = 800;
    private static final Integer FRAME_HEIGHT = 600;
    private JFrame previousFrame;

    public LoginFrame(JFrame previousFrame) {
        this.previousFrame = previousFrame;
        initUI();
    }

    private void initUI() {
        setTitle("聊天应用程序 - 登录");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        // 背景设置（可选：把 111.jpg 放在项目根目录即可加载）
        JLabel bgLabel = new JLabel(loadBackgroundImage());
        bgLabel.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        bgLabel.setLayout(null);
        add(bgLabel);
        // 组件初始化
        initComponents(bgLabel);
        setVisible(true);
    }

    private static ImageIcon loadBackgroundImage() {
        java.io.File file = new java.io.File("111.jpg");
        return file.isFile() ? new ImageIcon(file.getAbsolutePath()) : null;
    }

    private void initComponents(JLabel bgLabel) {
        // 标题
        JLabel titleLabel = new JLabel("用户登录");
        titleLabel.setBounds(225, 50, 350, 55);
        titleLabel.setFont(new Font("PingFang SC", Font.BOLD, 36));
        titleLabel.setForeground(Color.RED);
        bgLabel.add(titleLabel);

        // 用户名输入
        JTextField userField = createInputField(bgLabel, "用户名:", 130);
        // 密码输入
        JPasswordField passField = createPasswordField(bgLabel, "密码:", 190);
        //调用两个封装方法分别添加用户名和密码字段
        // 按钮组
        setupButtons(bgLabel, userField, passField);//会创建 3 个按钮：登录，注册，返回
    }

    private JTextField createInputField(JLabel parent, String label, int yPos) {
        JLabel jl = new JLabel(label);
        jl.setBounds(150, yPos, 110, 30);
        jl.setFont(new Font("PingFang SC", Font.BOLD, 18));
        jl.setForeground(Color.RED);
        parent.add(jl);
        
        JTextField field = new JTextField();
        field.setBounds(230, yPos, 190, 30);
        parent.add(field);
        return field;
    }

    private JPasswordField createPasswordField(JLabel parent, String label, int yPos) {
        JLabel jl = new JLabel(label);
        jl.setBounds(150, yPos, 110, 30);
        jl.setFont(new Font("PingFang SC", Font.BOLD, 18));
        jl.setForeground(Color.RED);
        parent.add(jl);
        
        JPasswordField field = new JPasswordField();
        field.setBounds(230, yPos, 190, 30);
        parent.add(field);
        return field;
    }

    private void setupButtons(JLabel bgLabel, JTextField userField, JPasswordField passField) {
        // 登录按钮
        JButton loginBtn = createButton("登录", 150, 250);
        loginBtn.addActionListener(e -> handleLogin(userField, passField));
        bgLabel.add(loginBtn);

        // 注册按钮
        JButton regBtn = createButton("注册", 250, 300);
        regBtn.addActionListener(e -> openRegister());
        bgLabel.add(regBtn);

        // 返回按钮
        JButton backBtn = createButton("返回", 345, 250);
        backBtn.addActionListener(e -> returnToPrevious());
        bgLabel.add(backBtn);
    }

    private JButton createButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 100, 30);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.RED);
        btn.setFont(new Font("PingFang SC", Font.BOLD, 14));
        return btn;
    }

    private void handleLogin(JTextField userField, JPasswordField passField) {
        String username = userField.getText();
        String password = new String(passField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("用户名和密码不能为空");
            return;
        }
        
        try {
            Socket socket = new Socket("127.0.0.1", 8888);//创建客户端 Socket 连接
            TransferInfo tfi = new TransferInfo();//封装用户输入的用户名和密码为 TransferInfo 对象
            tfi.setUsername(username);
            tfi.setPassword(password);
            tfi.setStatusEnum(InfoType.LOGIN);//设置登录类型
            IOStream.writeMessage(socket, tfi);//向服务器发送登录请求
            TransferInfo response = (TransferInfo) IOStream.readMessage(socket);//读取返回结果
            if (response.getLoginSuccess()) {//登录成功，启动消息接收线程，打开聊天窗口
                ClientHandler handler = new ClientHandler(socket, this);
                handler.start();
                SwingUtilities.invokeLater(() -> {
                    new ChatFrame(username, socket, handler);
                    dispose();//销毁当前登录界面
                });
            } else {
                showError("登录失败: " + response.getNotice());
                socket.close();
            }
        } catch (IOException ex) {
            showError("连接服务器失败: " + ex.getMessage());
        }
    }
// 注册和返回功能
    private void openRegister() {
        new RegisterFrame(this);
        dispose();
    }

    private void returnToPrevious() {
        dispose();
        if (previousFrame != null) {
            previousFrame.setVisible(true);
        }
    }
//错误提示框
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame(null));
    }
}
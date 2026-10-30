//用户注册窗口
package jiwangkeshe;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class RegisterFrame extends JFrame {
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private JFrame parentFrame; // 父窗口引用

    public RegisterFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        initUI();
    }
    private static ImageIcon loadBackgroundImage() {
        java.io.File file = new java.io.File("111.jpg");
        return file.isFile() ? new ImageIcon(file.getAbsolutePath()) : null;
    }

    private void initUI() {
        setTitle("用户注册");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        // 背景图片（可选：把 111.jpg 放在项目根目录即可加载）
        ImageIcon imageBg = loadBackgroundImage();
        JLabel jlbBg = new JLabel(imageBg);
        jlbBg.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        jlbBg.setLayout(null);
        add(jlbBg);
        // 标题
        JLabel lblTitle = new JLabel("用户注册");
        lblTitle.setBounds(225, 50, 350, 55);
        lblTitle.setFont(new Font("PingFang SC", Font.BOLD, 36));
        lblTitle.setForeground(Color.red);
        jlbBg.add(lblTitle);
        // 用户名输入
        JLabel lblUid = new JLabel("用户名:");
        lblUid.setBounds(250, 150, 80, 30);
        lblUid.setFont(new Font("PingFang SC", Font.BOLD, 18));
        lblUid.setForeground(Color.red);
        jlbBg.add(lblUid); 
        JTextField textUid = new JTextField();
        textUid.setBounds(350, 150, 200, 30);
        jlbBg.add(textUid);
        // 密码输入
        JLabel lblPwd = new JLabel("密码:");
        lblPwd.setBounds(250, 200, 80, 30);
        lblPwd.setFont(new Font("PingFang SC", Font.BOLD, 18));
        lblPwd.setForeground(Color.red);
        jlbBg.add(lblPwd);        
        JPasswordField textPwd = new JPasswordField();
        textPwd.setBounds(350, 200, 200, 30);
        jlbBg.add(textPwd);
        // 确认密码
        JLabel lblConfirm = new JLabel("确认密码:");
        lblConfirm.setBounds(250, 250, 100, 30);
        lblConfirm.setFont(new Font("PingFang SC", Font.BOLD, 18));
        lblConfirm.setForeground(Color.red);
        jlbBg.add(lblConfirm);     
        JPasswordField textConfirm = new JPasswordField();
        textConfirm.setBounds(350, 250, 200, 30);
        jlbBg.add(textConfirm);
        // 注册按钮
        JButton btnRegister = new JButton("提交注册");
        btnRegister.setBounds(300, 320, 120, 35);
        btnRegister.setBackground(Color.white);
        btnRegister.setForeground(Color.red);
        btnRegister.setFont(new Font("PingFang SC", Font.BOLD, 16));
        btnRegister.addActionListener(e -> {
            String username = textUid.getText();
            char[] pwdArray = textPwd.getPassword();
            char[] confirmArray = textConfirm.getPassword();
            String password = new String(pwdArray);
            String confirm = new String(confirmArray);
            // 输入验证
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 发送注册请求
            TransferInfo info = new TransferInfo();
            info.setUsername(username);
            info.setPassword(password);
            info.setStatusEnum(InfoType.REGISTER); // 设置状态为注册
            connectionServer(info);
        });
        jlbBg.add(btnRegister);
        // 返回登录按钮
        JButton btnBack = new JButton("返回登录");
        btnBack.setBounds(300, 370, 120, 35);
        btnBack.setBackground(Color.white);
        btnBack.setForeground(Color.red);
        btnBack.setFont(new Font("PingFang SC", Font.BOLD, 16));
        btnBack.addActionListener(e -> {
            dispose(); // 关闭注册窗口
            if (parentFrame != null) {
                parentFrame.setVisible(true); // 显示登录窗口
            }
        });
        jlbBg.add(btnBack);

        setVisible(true);
    }
    private void connectionServer(TransferInfo info) {
        try (Socket socket = new Socket("127.0.0.1", 8888)) {
            IOStream.writeMessage(socket, info);
            TransferInfo response = (TransferInfo) IOStream.readMessage(socket);
            
            if (response.getRegisterSuccess()) {
                JOptionPane.showMessageDialog(this, "注册成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                parentFrame.setVisible(true); // 返回登录界面
            } else {
                JOptionPane.showMessageDialog(this, "注册失败: " + response.getNotice(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "连接服务器失败", "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "数据解析错误", "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
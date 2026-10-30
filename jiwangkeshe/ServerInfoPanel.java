package jiwangkeshe;

import java.awt.*;
import javax.swing.*;

public class ServerInfoPanel {
    public JTextField txtServerName;    // 服务器名称
    public JTextField txtIP;            // IP地址
    public JTextField txtPort;          // 端口号

    public JPanel getServerInfoPanel() {
        // 主面板
        JPanel pnlServer = new JPanel();
        pnlServer.setOpaque(false);
        pnlServer.setLayout(null);
        pnlServer.setBounds(0, 0, ServerFrame.FRAME_WIDTH, ServerFrame.FRAME_HEIGHT);

        // 添加服务器参数面板
        pnlServer.add(getServerParam());

        return pnlServer;
    }

    public JPanel getServerParam() {
        JPanel serverParamPanel = new JPanel();
        serverParamPanel.setOpaque(false);
        serverParamPanel.setBounds(150, 100, 300, 250); // 可根据需要调整位置和大小
        serverParamPanel.setFont(new Font("PingFang SC", Font.BOLD, 15));
        serverParamPanel.setLayout(new GridLayout(3, 2, 10, 10));

        // 服务器名称
        JLabel lblServerName = new JLabel("NAME:");
        lblServerName.setForeground(Color.ORANGE);
        lblServerName.setFont(new Font("PingFang SC", Font.BOLD, 15));
        txtServerName = new JTextField(14);
        txtServerName.setFont(new Font("PingFang SC", Font.BOLD, 15));
        txtServerName.setEditable(false);
        txtServerName.setHorizontalAlignment(JTextField.CENTER);

        // IP 地址
        JLabel lblIP = new JLabel("IP:");
        lblIP.setForeground(Color.ORANGE);
        lblIP.setFont(new Font("PingFang SC", Font.BOLD, 15));
        txtIP = new JTextField(14);
        txtIP.setFont(new Font("PingFang SC", Font.BOLD, 15));
        txtIP.setEditable(false);
        txtIP.setHorizontalAlignment(JTextField.CENTER);

        // 端口号
        JLabel lblPort = new JLabel("PORT:");
        lblPort.setForeground(Color.ORANGE);
        lblPort.setFont(new Font("PingFang SC", Font.BOLD, 15));
        txtPort = new JTextField(Port.SERVER_PORT + "", 14);
        txtPort.setFont(new Font("PingFang SC", Font.BOLD, 15));
        txtPort.setEditable(false);
        txtPort.setHorizontalAlignment(JTextField.CENTER);

        serverParamPanel.add(lblServerName);
        serverParamPanel.add(txtServerName);
        serverParamPanel.add(lblIP);
        serverParamPanel.add(txtIP);
        serverParamPanel.add(lblPort);
        serverParamPanel.add(txtPort);

        return serverParamPanel;
    }
}

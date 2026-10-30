package jiwangkeshe;

import javax.swing.*;
import java.awt.*;

public class ServerFrame extends JFrame {
    private static final long serialVersionUID = -8235369520375669451L;
    public static final int FRAME_WIDTH = 600;
    public static final int FRAME_HEIGHT = 600;

    public ServerInfoPanel serverInfoPanel;

    public ServerFrame() {
        setTitle("SERVER");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 选项卡：只保留服务器信息
        JTabbedPane tpServer = new JTabbedPane(JTabbedPane.TOP);
        tpServer.setBackground(Color.WHITE);
        tpServer.setFont(new Font("PingFang SC", Font.BOLD, 20));

        serverInfoPanel = new ServerInfoPanel();
        tpServer.add("SERVER INFO", serverInfoPanel.getServerInfoPanel());

        add(tpServer);
        setVisible(true);
    }
}


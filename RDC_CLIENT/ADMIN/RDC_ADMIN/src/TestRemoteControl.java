import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class TestRemoteControl extends JFrame {

    // TODO: Rename class later..

    public volatile BufferedImage screen;

    public TestRemoteControl(String key, String targetIP) throws Exception {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(1000, 600);
        setResizable(false);
        setVisible(true);

        Thread remoteControlHandler = new Thread(new RemoteControlHandler(key, targetIP, this));
        remoteControlHandler.start();

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (screen != null)
            g.drawImage(screen, 0, 0, screen.getWidth(), screen.getHeight(), null);

    }

}

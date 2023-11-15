import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class TestRemoteControl extends JFrame {

    // TODO: Rename class later..
    public ScreenDisplayer screen;

    public TestRemoteControl(String key, String targetIP) {

        screen = new ScreenDisplayer();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(1000, 600);
        setResizable(false);
        add(screen);
        setVisible(true);

        Thread remoteControlHandler = new Thread(new RemoteControlHandler(key, targetIP, this));
        remoteControlHandler.start();

    }

    public class ScreenDisplayer extends JPanel {
        private BufferedImage screenFrame;

        public ScreenDisplayer() {
            setSize(1000, 600);
        }

        @Override
        public void paint(Graphics g) {

            if (screenFrame != null)
                g.drawImage(screenFrame, 0, 0, null);

        }

        public void display(BufferedImage screenFrame) {
            this.screenFrame = screenFrame;
            repaint();
        }

    }

}

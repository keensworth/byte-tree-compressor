import javax.swing.*;
import java.awt.*;

public class WindowMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                Window newWindow = new Window();

                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                int w = newWindow.getSize().width;
                int h = newWindow.getSize().height;
                int x = (dim.width-w)/2;
                int y = (dim.height-h)/2;

                newWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                newWindow.setLocation(x, y);
                newWindow.setVisible(true);
            }
        });
    }
}

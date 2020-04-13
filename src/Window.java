import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Window extends JFrame {
    private JFrame frame;
    private JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JButton comp_chooseFileButton;
    private JButton decomp_chooseFileButton;
    private JButton comp_changeDestinationButton;
    private JButton decomp_changeDestinationButton;
    private JButton decomp_beginButton;
    private JButton comp_beginButton;
    private JLabel compressLabel1;
    private JLabel compressLabel2;
    private JLabel decompressLabel1;
    private JLabel decompressLabel2;

    private String filePath, fileName, fileDirectory;

    public Window(){
        add(panel1);
        setTitle("Byte Tree Compressor");
        setSize(550,150);

        addListeners();

    }

    void addListeners(){
        //Compression [Choose file]  &  [Change destination]  buttons
        comp_chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                filePath = pickFile();
                compressLabel1.setText(fileName);

                fileDirectory = filePath.substring(0, filePath.length()-4) + "_out.bt";
                compressLabel2.setText(fileDirectory);
            }
        });

        comp_changeDestinationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fileDirectory = pickDestination();

                fileDirectory += ".bt";
                compressLabel2.setText(fileDirectory);
            }
        });

        //Decompression [Choose file]  &  [Change destination]  buttons
        decomp_chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                filePath = pickFile();
                decompressLabel1.setText(fileName);

                fileDirectory = filePath.substring(0, filePath.length()-3) + ".png";
                decompressLabel2.setText(fileDirectory);
            }
        });


        decomp_changeDestinationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fileDirectory = pickDestination();

                fileDirectory += ".png";
                decompressLabel2.setText(fileDirectory);
            }
        });


        //Begin Buttons
        comp_beginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (filePath==null){
                    frame = new JFrame("Error");
                    frame.setSize(75,50);
                    JOptionPane.showMessageDialog(frame, "No input file chosen.");
                } else {
                    try {
                        TreeForest.compress(filePath, fileDirectory);
                    } catch (IOException e){ }
                    filePath = null;
                    fileName = null;
                    fileDirectory = null;
                }
            }
        });

        decomp_beginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (filePath==null){
                    frame = new JFrame("Error");
                    frame.setSize(75,50);
                    JOptionPane.showMessageDialog(frame, "No input file chosen.");
                } else {
                    try {
                        TreeForest.decompress(filePath, fileDirectory);
                    } catch (IOException e){ }
                    filePath = null;
                    fileName = null;
                    fileDirectory = null;
                }
            }
        });
    }

    String pickDestination(){
        JFrame testFrame = new JFrame("TreeCompress");
        FileDialog fd = new FileDialog(testFrame, "Choose a destination", FileDialog.SAVE);
        fd.setDirectory("C:\\");
        fd.setFile("");
        fd.setVisible(true);
        fileName = fd.getFile();
        fileDirectory = fd.getDirectory();
        return fileDirectory + fileName;
    }

    String pickFile(){
        JFrame testFrame = new JFrame("TreeCompress");
        FileDialog fd = new FileDialog(testFrame, "Choose a file (.jpg or .png)", FileDialog.LOAD);
        fd.setDirectory("C:\\");
        fd.setFile("");
        fd.setVisible(true);
        fileName = fd.getFile();
        fileDirectory = fd.getDirectory();

        return fileDirectory + fileName;
    }
}

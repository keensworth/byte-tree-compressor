//This work is licensed under the Creative Commons
// Attribution-NonCommercial-NoDerivs 3.0 Unported License.
// To view a copy of this license, visit
// http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter
// to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

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
        setSize(700,150);

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
                        Compress compression = new Compress(filePath, fileDirectory);
                        compression.compress();

                        frame = new JFrame("Compression Complete");
                        frame.setSize(100,50);

                        long rawSize = compression.getRawSize();
                        long compressedSize = compression.getCompressedSize();

                        double ratio = (((double)compressedSize/(double)rawSize)*100);
                        BigDecimal bd = new BigDecimal(ratio);
                        bd = bd.round(new MathContext(4));
                        double rounded = bd.doubleValue();

                        JOptionPane.showMessageDialog(frame, "RAW Size: (width * height * 3 bytes/pixel)\n"+ String.format("%,d", rawSize) + " bytes\n\nCompressed Size:\n"+ String.format("%,d", compressedSize) +" bytes\n\n"+rounded+"% of original");
                    } catch (IOException e){ }
                    filePath = null;
                    fileName = null;
                    fileDirectory = null;
                    compressLabel1.setText("None selected");
                    compressLabel2.setText("None selected");
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
                        Decompress decompression = new Decompress(filePath, fileDirectory);
                        decompression.decompress();
                    } catch (IOException e){ }
                    filePath = null;
                    fileName = null;
                    fileDirectory = null;
                    decompressLabel1.setText("None selected");
                    decompressLabel2.setText("None selected");
                }
            }
        });
    }

    private String pickDestination(){
        JFrame testFrame = new JFrame("ByteTreeCompress");
        FileDialog fd = new FileDialog(testFrame, "Choose a destination", FileDialog.SAVE);
        fd.setDirectory("C:\\");
        fd.setFile("");
        fd.setVisible(true);
        fileName = fd.getFile();
        fileDirectory = fd.getDirectory();
        return fileDirectory + fileName;
    }

    private String pickFile(){
        JFrame testFrame = new JFrame("ByteTreeCompress");
        FileDialog fd = new FileDialog(testFrame, "Choose a file", FileDialog.LOAD);
        fd.setDirectory("C:\\");
        fd.setFile("");
        fd.setVisible(true);
        fileName = fd.getFile();
        fileDirectory = fd.getDirectory();

        return fileDirectory + fileName;
    }
}

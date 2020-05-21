import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Compress {
    private int height,width,pixels;
    private int orders;
    private String inputFile, outputFilePath;
    private int bitDepth = 24; //pixel bit depth
    private boolean[] inverted;
    private boolean[][] inProgress;
    private int[] logarithmicCounter;
    private ByteTree[] byteForest;
    private BufferedImage image;


    public Compress(String inputFile, String outputFilePath) throws IOException{
        this.inputFile = inputFile;
        this.outputFilePath = outputFilePath;

        this.image = initImage(this.inputFile);

        this.orders = (int)Math.ceil(Math.log(pixels)/Math.log(8));

        this.inProgress = new boolean[bitDepth][orders];
        this.inverted = invertLayers();
        this.byteForest = Utility.initByteForest(bitDepth,pixels,orders);
    }


    void compress() throws IOException{
        growForest();
        writeToFile();
    }


    private void growForest(){
         for (int tree = 0; tree < bitDepth; tree++) {
            this.logarithmicCounter = new int[orders];
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int bit = ((image.getRGB(x, y))>>>((bitDepth-1)-tree))&0b1;
                    if ((inverted[tree] && bit==0) || (!this.inverted[tree] && bit==1)){
                        treeUpdate(0,tree);
                    }
                    incLogarithmicCounter(0,8);
                }
            }
        }
    }


    private void treeUpdate(int order, int tree){
        if (inProgress[tree][order]){ //chunk already in progress //update the bit at logarithmicCounter[order]
            this.byteForest[tree].setData(order, logarithmicCounter[order]);
        }
        else {
            //Start new byte
            this.inProgress[tree][order] = true;
            this.byteForest[tree].setData(order, logarithmicCounter[order]);

            //Build higher order tree
            if (order < orders - 1)
                treeUpdate(order + 1, tree);
        }
    }


    private void incLogarithmicCounter(int order, int modulo){
        this.logarithmicCounter[order]++;
        if (this.logarithmicCounter[order]==modulo && order < orders-1){
            this.logarithmicCounter[order]=0;
            for (int tree = 0; tree < bitDepth; tree++){
                if (inProgress[tree][order]){
                    this.inProgress[tree][order]=false;
                    this.byteForest[tree].incWriteIndex(order);
                }
            }
            incLogarithmicCounter(order+1,modulo);
        }
    }


    private void writeToFile() throws IOException{
        File f = new File(outputFilePath);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));

        byte[] dimensions = new byte[4];

        dimensions[0] = (byte)((width>>>8)&0xff); //Width
        dimensions[1] = (byte)((width)&0xff);     //Width
        dimensions[2] = (byte)((height>>>8)&0xff);//Height
        dimensions[3] = (byte)((height)&0xff);    //Height

        out.write(dimensions);
        for (int tree = 0; tree<bitDepth; tree++){
            out.write(this.byteForest[tree].condenseByteTree(inverted[tree]));
        }
        out.close();
    }


    private BufferedImage initImage(String inputFile) throws IOException {
        File input = new File(inputFile);
        BufferedImage image = ImageIO.read(input);

        //Set image dimensions
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.pixels = width * height;
        return image;
    }


    private boolean[] invertLayers(){
        boolean[] array = new boolean[bitDepth];
        for (int layer = 0; layer < bitDepth; layer++) {
            int count = 0;
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int bit = ((image.getRGB(x, y))>>>((bitDepth-1)-layer))&0b1;
                    if (bit==1){
                        count++;
                    }
                }
            }
            if (count>(pixels/2)){
                array[layer] = true;
            }
        }
        return array;
    }
}

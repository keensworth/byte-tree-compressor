//This work is licensed under the Creative Commons
// Attribution-NonCommercial-NoDerivs 3.0 Unported License.
// To view a copy of this license, visit
// http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter
// to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Compress {
    private int height,width,pixels;          //Image properties
    private int orders;                       //Orders in the tree
    private String inputFile, outputFilePath;
    private int bitDepth = 24;                //Pixel bit depth (8 bits for R, G, and B each)
    private boolean[] inverted;               //Array to keep track of which bit layers are inverted
    private boolean[][] inProgress;           //Double array to keep track of which orders in each byteTree are in progress
    private int[] logarithmicCounter;         //Logarithmic counter (indexed by tree order)
    private ByteTree[] byteForest;            //byteTree array
    private BufferedImage image;

    //Initializer
    public Compress(String inputFile, String outputFilePath) throws IOException{
        this.inputFile = inputFile;
        this.outputFilePath = outputFilePath;

        this.image = initImage(this.inputFile);

        this.orders = (int)Math.ceil(Math.log(pixels)/Math.log(8));

        this.inProgress = new boolean[bitDepth][orders];
        this.inverted = invertLayers();
        this.byteForest = Utility.initByteForest(bitDepth,pixels,orders);
    }

    //Begin compression
    void compress() throws IOException{
        growForest();
        writeToFile();
    }

    //Build ByteTree array with data from image
    private void growForest(){
         for (int tree = 0; tree < bitDepth; tree++) {
            this.logarithmicCounter = new int[orders]; //reset counter

             //Scan over imge
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {

                    //Set bit from current bit layer (tree)
                    int bit = ((image.getRGB(x, y))>>>((bitDepth-1)-tree))&0b1;

                    if ((inverted[tree] && bit==0) || (!this.inverted[tree] && bit==1)){
                        //build higher order tree
                        treeUpdate(0,tree);
                    }

                    incLogarithmicCounter(0,8);
                }
            }
        }
    }

    //builds tree from raw data
    private void treeUpdate(int order, int tree){
        if (inProgress[tree][order]){ //chunk already in progress //update the bit at logarithmicCounter[order]
            this.byteForest[tree].setData(order, logarithmicCounter[order]);
        }
        else { //Start new byte
            this.inProgress[tree][order] = true;
            this.byteForest[tree].setData(order, logarithmicCounter[order]);

            //Build higher order tree
            if (order < orders - 1)
                treeUpdate(order + 1, tree);
        }
    }

    //Logarithmic counter to assist tree building
    private void incLogarithmicCounter(int order, int modulo){
        this.logarithmicCounter[order]++;

        if (this.logarithmicCounter[order]==modulo && order < orders-1){ //reset counter at [order]
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

    //write byteForest to .bt file
    private void writeToFile() throws IOException{
        File f = new File(outputFilePath);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));

        byte[] dimensions = new byte[4];

        //Set width / height tags
        dimensions[0] = (byte)((width>>>8)&0xff); //Width
        dimensions[1] = (byte)((width)&0xff);     //Width
        dimensions[2] = (byte)((height>>>8)&0xff);//Height
        dimensions[3] = (byte)((height)&0xff);    //Height

        out.write(dimensions);
        //Write byteForest[tree] to .bt file
        for (int tree = 0; tree<bitDepth; tree++){
            out.write(this.byteForest[tree].condenseByteTree(inverted[tree]));
        }
        out.close();
    }

    //Open image and initialize properties
    private BufferedImage initImage(String inputFile) throws IOException {
        File input = new File(inputFile);
        BufferedImage image = ImageIO.read(input);

        //Set image dimensions
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.pixels = width * height;
        return image;
    }

    //Count bits in each bit layer, and set invert if greater than 50% are '1'
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

    //Return initial image size (calculated)
    int getRawSize(){
        return (this.pixels*3);
    }

    //Return compressed size (size of .bt file)
    int getCompressedSize(){
        int size = 0;
        for (int tree = 0; tree<bitDepth; tree++){
            size += this.byteForest[tree].getCondensedSize();
        }
        return size;
    }
}

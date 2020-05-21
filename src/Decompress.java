import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Decompress {
    private String inputFile, outputFilePath;
    private int height, width, pixels;
    private int orders;
    private int bitDepth = 24; //pixel bit depth
    private boolean[] inverted;
    private BufferedInputStream sourceFile;
    private BufferedImage image;
    private int[] pixelArray;
    private ByteTree[] byteForest;


    public Decompress(String inputFile, String outputFilePath) throws IOException {
        this.inputFile = inputFile;
        this.outputFilePath = outputFilePath;

        this.sourceFile = initFile();

        this.orders = (int) Math.ceil(Math.log(pixels) / Math.log(8));
        this.inverted = new boolean[bitDepth];

        this.byteForest = Utility.initByteForest(bitDepth, pixels, orders);
    }


    void decompress() throws IOException {
        parseSourceFile();

        this.pixelArray = initPixelArray();
        for (int tree = 0; tree < bitDepth; tree++) {
            populatePixelArray(tree, orders - 1, 0);
        }

        this.image = buildImage();

        writeToFile();
    }


    private BufferedInputStream initFile() throws IOException {
        FileInputStream currFile = new FileInputStream(inputFile);
        BufferedInputStream currentStream = new BufferedInputStream(currFile);

        int width1 = currentStream.read();
        int width2 = currentStream.read();
        int height1 = currentStream.read();
        int height2 = currentStream.read();

        this.width = ((width1 << 8) | width2) & 0xffff; //Image width
        this.height = ((height1 << 8) | height2) & 0xffff; //Image height

        this.pixels = width * height;

        return currentStream;
    }


    private void parseSourceFile() throws IOException {
        for (int tree = 0; tree < bitDepth; tree++) {
            byte size1 = (byte) sourceFile.read();
            byte size2 = (byte) sourceFile.read();
            byte size3 = (byte) sourceFile.read();
            byte size4 = (byte) sourceFile.read();

            boolean inverted = ((size1 >>> 8) & 0b1) == 1;
            this.inverted[tree] = inverted;

            int bufferSize = (size1 << 24) | (size2 << 16) | (size3 << 8) | (size4);

            byte[] tempBuffer = new byte[bufferSize];
            sourceFile.read(tempBuffer);

            byteForest[tree].toByteTree(tempBuffer, inverted);
        }
    }


    private int[] initPixelArray() {
        int tempMask = 0b100000000000000000000000;
        int baseMask = 0b000000000000000000000000;

        //Set the base mask to account for appropriate inverted trees
        for (int tree = 0; tree < bitDepth; tree++) {
            if (inverted[tree]) {
                baseMask |= (tempMask >>> tree);
            }
        }

        int[] tempPixelArray = new int[pixels];

        //Initialize pixelArray with the base mask
        for (int tempPixel = 0; tempPixel < pixels; tempPixel++) {
            tempPixelArray[tempPixel] = baseMask;
        }

        return tempPixelArray;
    }


    private void populatePixelArray(int tree, int order, int previousIndex) {
        int mask = 0b100000000000000000000000;

        if (order >= 0 || previousIndex < pixels) {
            byte tempByte = byteForest[tree].getData(order);

            //Scan over byte and either:   1) keep moving down tree   2) build colorArray if order==0
            for (int bit = 0; bit < 8; bit++) {

                if (previousIndex + bit < pixels && ((tempByte >>> (7 - bit)) & 0b1) == 1) { //relevant information in tree

                    //if order is 0, build colorArray, else keep moving downwards in tree
                    if (order == 0) {

                        //if flip is true, a '1' in the base of the tree indicates that the raw data should be a 0 there
                        if (inverted[tree]) {
                            pixelArray[previousIndex + bit] &= (mask >>> tree) ^ 0xff;
                        } else {
                            pixelArray[previousIndex + bit] |= (mask >>> tree);
                        }

                    } else {
                        populatePixelArray(tree, order - 1, bit * (int) Math.pow(9, order) + previousIndex);
                    }
                }
            }
        }
    }


    private BufferedImage buildImage() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currColor = pixelArray[y * width + x] | 0xff000000;

                img.setRGB(x, y, currColor);
            }
        }

        return img;
    }


    private void writeToFile() {
        try {
            File f = new File(outputFilePath);
            ImageIO.write(image, "png", f);
        } catch (IOException e) {
        }
    }
}

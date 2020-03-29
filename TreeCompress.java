import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;


class TreeCompress{
    //-------------Arbitrary binary------------------------|
    static int data; //change to mxn                       |
    //private static double probability = (float) (1/64);//|
    // static int[] binArray;                            //|
    //-----------------------------------------------------|


    static int mapDepth = 24; //768 RGB - 256 bit coupling - 96 half bytes
    static int maxOrder;

    static ByteTree[] treeArray;
    private static int[] colorArray;
    static int[] buildOrderIndex;
    static int[] orderTempIndex;
    static int[][] reconIndex;
    static int[][] lastUpdateIndex;
    static boolean[][] preserve;
    static boolean[][] inProgress;
    //Counting Functions
    static int[][] bitCount;
    static int[][] groupCount;
    private static boolean[] reverse = new boolean[mapDepth];

    static int presentCount;
    static boolean lastRun = false;



    static void decompressData(int kk, int width, int height){
        colorArray = new int[data];
        int tempMask = 0b100000000000000000000000;
        int baseMask = 0b000000000000000000000000;

        //init colorArray w/ binary literals (set corresponding reverse[] bits to 1, else 0)
        for(int tempMap = 0; tempMap<mapDepth; tempMap++){
            if (reverse[tempMap]){
                baseMask |= (tempMask>>>tempMap);
            }
        }
        for (int tempPixel = 0; tempPixel<data; tempPixel++){
            colorArray[tempPixel] = baseMask;
        }

        //TODO: option for alpha channel
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);;

        //Build one dimensional color array (to build image with)
        char compareChar;
        boolean flip;
        for(int tempMap = 0; tempMap<mapDepth; tempMap++){
            flip = reverse[tempMap];
            buildOrderIndex[0]=0;
            fillColorArray(kk,tempMap,maxOrder-2, flip,0);
        }

        //Draw decompressed data to img using colorArray values
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++)
            {
                //check colorArray for pixel's value
                int currColor = colorArray[y*width+x] | 0xff000000;
                //set pixel
                img.setRGB(x, y, currColor);
            }
        }

        //Write image to disk
        try
        {
            File f = new File("OUTPUT_PATH/file_name.png");
            ImageIO.write(img, "png", f);
        }
        catch(IOException e)
        { }
    }

    static void fillColorArray(int kk, int mapping, int order, boolean flip, int lastIndex){
        int mask = 0b100000000000000000000000;
        if (order==-1 || lastIndex>=data){
        }
        else{
            byte tempByte = treeArray[mapping].getData(order,reconIndex[mapping][order]);
            //Scan over byte and either   1) keep moving down tree   2) build color array if order=0
            for (int tempK = 0; tempK<kk; tempK++){
                if (lastIndex+tempK<data && ((tempByte>>>(7-tempK))&0b1)==1){ //relevant information in tree

                    //if order is 0, build colorArray, else keep moving downwards in tree
                    if (order==0){

                        //if flip is true, a '1' in the base of the tree indicates that the raw data should be a 0 there
                        if (flip){ colorArray[lastIndex+tempK] &= ~(mask>>>mapping); }
                        else { colorArray[lastIndex+tempK] |= (mask>>>mapping); }
                    }

                    else { fillColorArray(kk,mapping,order-1, flip, tempK*(int)Math.pow(kk, order)+lastIndex); }
                }
            }
            reconIndex[mapping][order]++;
        }
    }

    static void compressData(int kk, int width, int height, BufferedImage image){
        //scan over binArray and create compression tree accordingly
        int order = 0;
        int tempColor;
        int[] colorArray = new int[3];


        //Scan image, obtain RGB values, and map values into compression function appropriately
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = new Color(image.getRGB(j, i));
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();

                colorArray[0]=red;
                colorArray[1]=green;
                colorArray[2]=blue;

                presentCount+=3;


                //Bits - 24 ----------------------------------------------------------------------------

                //Scan the bits for each R, G, and B.
                int counter = 0;
                //color 0-2, for each color
                for (int color = 0; color<3;color++){
                    for(int slide = 1; slide<9; slide++){
                        tempColor = (colorArray[color]>>>(8-slide))&1; //move across respective color byte and call tree function w/ respect to reverse
                        if (reverse[counter]){ //work with 0's to call function
                            if (tempColor==0){
                                treeUpdate(order,kk,counter);
                            }
                        }
                        else {
                            if (tempColor == 1) { //work with 1's to call function
                                treeUpdate(order, kk, counter);
                            }
                        }
                        counter++;
                    }
                }
                //update orderTempIndex
                orderTempIndexUpdate(order,kk);
            }
        }
    }

    private static void treeUpdate(int order, int kk, int mapping){
        if (preserve[mapping][order]){ //chunk already in progress //update the bit at orderTempIndex[order]
            treeArray[mapping].setData(order,orderTempIndex[order]);
        }

        else{
            //Finish previous byte
            if (inProgress[mapping][order]){
                treeArray[mapping].incIndex(order);
            }

            //Start new byte
            treeArray[mapping].setData(order,orderTempIndex[order]);

            if (orderTempIndex[order]<kk-1){
                inProgress[mapping][order] = true;
                preserve[mapping][order] = true;
            }

            //Build higher order tree
            if (order<maxOrder-1) {
                treeUpdate(order + 1, kk, mapping);
            }
        }

        if (orderTempIndex[order] == kk-1){
            inProgress[mapping][order] = false;
            treeArray[mapping].incIndex(order);
        }
    }

    private static void orderTempIndexUpdate(int order, int kk){
        orderTempIndex[order]++;
        if (orderTempIndex[order]==kk && order < maxOrder-1){
            orderTempIndex[order]=0;
            for (int tempMap = 0; tempMap<mapDepth; tempMap++){
                preserve[tempMap][order]=false;
            }
            orderTempIndexUpdate(order+1,kk);
        }
    }

    static void reverseAnalysis(BufferedImage image, int width, int height){
        int tempColor;
        //for RGB bits: index 0 is r7, index 23 is b0
        int[] bitCount = new int[mapDepth];
        int totalData = width*height;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = new Color(image.getRGB(j, i));
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();

                int counter = 0;
                for (int r = 1; r<9;r++){
                    tempColor = (red>>>(8-r))&1;
                    if (tempColor==1){
                        bitCount[counter]++;
                    }
                    counter++;
                }
                for (int g = 1; g<9;g++){
                    tempColor = (green>>>(8-g))&1;
                    if (tempColor==1){
                        bitCount[counter]++;
                    }
                    counter++;
                }
                for (int b = 1; b<9;b++){
                    tempColor = (blue>>>(8-b))&1;
                    if (tempColor==1){
                        bitCount[counter]++;
                    }
                    counter++;
                }

            }
        }
        for (int scan = 0; scan < 24; scan++){
            if (bitCount[scan] > (totalData/2)){
                reverse[scan]=true;
            }
        }
    }

    static void writeToFile(byte[] byteArr, int mapping){
        File file = new File("C:/Users/Sargy/IdeaProjects/Compression/resources/output/outfull"+mapping+".bin");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(byteArr);
            System.out.println("Wrote "+mapping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


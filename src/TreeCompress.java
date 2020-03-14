import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;


class TreeCompress{
    //-------------Arbitrary binary----------------------|
    static int data; //change to mxn                     |
    private static double probability = (float) (1/64);//|
    static int[] binArray;                             //|
    //---------------------------------------------------|


    static int mapDepth = 24; //768 RGB - 256 bit coupling - 96 half bytes
    static int maxOrder;

    //static StringBuilder[][] stringArray;
    static ByteTree[] treeArray;
    static int[] colorArray;
    static int[] buildOrderIndex;
    static int[] orderTempIndex;
    static int[][] lastUpdateIndex;
    static boolean[][] preserve;
    static boolean[][] inProgress;
    //Counting Functions
    static int[][] bitCount;
    static int[][] groupCount;
    static int[] hashCount = new int[mapDepth];
    static boolean[] reverse = new boolean[mapDepth];

    static int presentCount;
    //private static double startTime;
    static boolean lastRun = false;

    static StringBuilder rawData = new StringBuilder(64);



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
        //File f = null;

        //Build one dimensional color array (to build image with)
        char compareChar;
        boolean flip;
        for(int tempMap = 0; tempMap<mapDepth; tempMap++){
            if (reverse[tempMap]){
                flip = true;
            } else { flip = false; }
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
                //System.out.println(Integer.toBinaryString(currColor));
                //System.out.println(reverse[0]);
            }
        }

        // write image to disk
        try
        {
            File f = new File("C:/Users/Sargy/IdeaProjects/Compression/resources/test_out.png");
            //f = new File("../resources/output4.jpg");
            ImageIO.write(img, "png", f);
            System.out.println("Image written");
        }
        catch(IOException e)
        {
            System.out.println("get fucked");
        }
    }

    static void fillColorArray(int kk, int mapping, int order, boolean flip, int lastIndex){
        int mask = 0b100000000000000000000000;
        if (order==-1 || lastIndex>=data){
            return;
        }
        else{
            for (int tempK = 0; tempK<kk; tempK++){
                if (stringArray[mapping][order].length()>0 && lastIndex+tempK<data && stringArray[mapping][order].charAt(tempK)=='1'){ //relevant information in tree
                    //if order is 0, build colorArray, else keep moving downwards in tree
                    if (order==0){
                        //if flip is true, a '1' in the base of the tree indicates that the raw data should be a 0 there
                        if (flip){ colorArray[lastIndex+tempK] &= ~(mask>>>mapping); } else { colorArray[lastIndex+tempK] |= (mask>>>mapping); }
                    } else { fillColorArray(kk,mapping,order-1, flip, tempK*(int)Math.pow(kk, order)+lastIndex); }
                }
            }
            //Delete chunks of stringArray as you scan (will be easier with bytes)
            stringArray[mapping][order].delete(0,kk);
        }
    }

    static void compressData(int kk, int width, int height, BufferedImage image){
        //scan over binArray and create compression tree accordingly
        int order = 0;
        int tempColor,tempColor2,tempColorC;
        //int red2,green2,blue2;
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
                                hashCount[counter]++;
                            }
                        }
                        else {
                            if (tempColor == 1) { //work with 1's to call function
                                treeUpdate(order, kk, counter);
                                hashCount[counter]++;
                            }
                        }
                        counter++;
                    }
                }
                //update orderTempIndex
                orderTempIndexUpdate(order,kk);

                //Two-Bits - 36 ------------------------------------------------------------------------
                /*
                int counter=0;
                for (int r = 1; r<5;r++){
                    tempColor = (red>>>(8-2*r))&0b11;
                    if (tempColor!=0){
                        treeUpdate(order,kk,tempColor+3*counter);
                        hashCount[tempColor+3*counter]++;
                    }
                    counter++;
                }
                for (int g = 1; g<5;g++){
                    tempColor = (green>>>(8-2*g))&0b11;
                    if (tempColor!=0){
                        treeUpdate(order,kk,tempColor+3*counter);
                        hashCount[tempColor+3*counter]++;
                    }
                    counter++;
                }
                for (int b = 1; b<5;b++){
                    tempColor = (blue>>>(8-2*b))&0b11;
                    if (tempColor!=0){
                        treeUpdate(order,kk,tempColor+3*counter);
                        hashCount[tempColor+3*counter]++;
                    }
                    counter++;
                }

                orderTempIndexUpdate(order,kk);

                 */

                //Custom - 64 --------------------------------------------------------------------------
                /*
                int p7 = ((red>>>7)<<2)     | ((green>>>7)<<1)     | ((blue>>>7));
                int p6 = (((red>>>6)&1)<<2) | (((green>>>6)&1)<<1) | ((blue>>>6)&1);
                int p5 = (((red>>>5)&1)<<2) | (((green>>>5)&1)<<1) | ((blue>>>5)&1);
                int p4 = (((red>>>4)&1)<<2) | (((green>>>4)&1)<<1) | ((blue>>>4)&1);
                int p3 = (((red>>>3)&1)<<2) | (((green>>>3)&1)<<1) | ((blue>>>3)&1);
                int p2 = (((red>>>2)&1)<<2) | (((green>>>2)&1)<<1) | ((blue>>>2)&1);
                int p1 = (((red>>>1)&1)<<2) | (((green>>>1)&1)<<1) | ((blue>>>1)&1);
                int p0 = (((red)&1)<<2)    | (((green)&1)<<1)    | ((blue)&1);

                treeUpdate(order,kk,p7);
                treeUpdate(order,kk,p6+8);
                treeUpdate(order,kk,p5+16);
                treeUpdate(order,kk,p4+24);
                treeUpdate(order,kk,p3+32);
                treeUpdate(order,kk,p2+40);
                treeUpdate(order,kk,p1+48);
                treeUpdate(order,kk,p0+56);

                orderTempIndexUpdate(order,kk);

                hashCount[p7]++;
                hashCount[p6+8]++;
                hashCount[p5+16]++;
                hashCount[p4+24]++;
                hashCount[p3+32]++;
                hashCount[p2+40]++;
                hashCount[p1+48]++;
                hashCount[p0+56]++;

                 */

                //Combo - 72 ---------------------------------------------------------------------------
                /*
                int red1 = (red>>>4);
                int red2 = (red>>>4) & 0b11;
                int red3 = (red>>>2) & 0b11;
                int green1 = (green>>>4);
                int green2 = (green>>>2) & 0b11;
                int green3 = (green) & 0b11;
                int blue1 = (blue>>>4);
                int blue2 = (blue>>>2) & 0b11;
                int blue3 = (blue) & 0b11;

                treeUpdate(order, kk, red1);
                treeUpdate(order, kk, red2 + 16);
                treeUpdate(order, kk, red3 + 20);
                treeUpdate(order, kk, green1 + 24);
                treeUpdate(order, kk, green2 + 40);
                treeUpdate(order, kk, green3 + 44);
                treeUpdate(order, kk, blue1 + 48);
                treeUpdate(order, kk, blue2 + 64);
                treeUpdate(order, kk, blue3 + 68);

                orderTempIndexUpdate(order,kk);

                hashCount[red1]++;
                hashCount[red2 + 16]++;
                hashCount[red3 + 20]++;
                hashCount[green1 + 24]++;
                hashCount[green2 + 40]++;
                hashCount[green3 + 44]++;
                hashCount[blue1 + 48]++;
                hashCount[blue2 + 64]++;
                hashCount[blue3 + 68]++;

                 */

                //Half Bytes - 96 ----------------------------------------------------------------------
                /*
                int highRed = red>>>4;
                int lowRed = red & 0b1111;
                int highGreen = green>>>4;
                int lowGreen = green & 0b1111;
                int highBlue = blue>>>4;
                int lowBlue = blue & 0b1111;

                treeUpdate(order, kk, highRed);
                treeUpdate(order, kk, lowRed + 16);
                treeUpdate(order, kk, highGreen + 32);
                treeUpdate(order, kk, lowGreen + 48);
                treeUpdate(order, kk, highBlue + 64);
                treeUpdate(order, kk, lowBlue + 80);

                orderTempIndexUpdate(order,kk);

                hashCount[highRed]++;
                hashCount[lowRed + 16]++;
                hashCount[highGreen + 32]++;
                hashCount[lowGreen + 48]++;
                hashCount[highBlue + 64]++;
                hashCount[lowBlue + 80]++;

                 */

                //Bit Coupling - 256 -------------------------------------------------------------------
                /*
                int firstOrder =  (red & 0b11000000)>>>2 | (green & 0b11000000)>>>4 | (blue & 0b11000000)>>>6;
                int secondOrder = (red & 0b110000)      | (green & 0b110000)>>>2   | (blue & 0b110000)>>>4;
                int thirdOrder =  (red & 0b1100)<<2     | (green & 0b1100)        | (blue & 0b1100)>>>2;
                int fourthOrder = (red & 0b11)<<4       | (green & 0b11)<<2       | (blue & 0b11);

                treeUpdate(order, kk, firstOrder);
                treeUpdate(order, kk, secondOrder + 64);
                treeUpdate(order, kk, thirdOrder + 128);
                treeUpdate(order, kk, fourthOrder + 192);

                orderTempIndexUpdate(order,kk);

                 */

                // Individual RGB - 768 ----------------------------------------------------------------
                /*
                treeUpdate(order, kk, red);         //red hash
                treeUpdate(order, kk, green + 256); //green hash
                treeUpdate(order, kk, blue + 512);  //blue hash

                orderTempIndexUpdate(order, kk);

                 */
            }

        }
        /*
        if (kk==2) {
            for (int c = 0; c < mapDepth; c++) {
                System.out.println((c%256)+" "+colorCount[c]);
            }
            System.out.println(pixelCount);
        }
         */
    }

    private static void treeUpdate(int order, int kk, int mapping){
        if (preserve[mapping][order]){ //chunk in progress
            while (lastUpdateIndex[mapping][order] + 1 < orderTempIndex[order]){
                stringArray[mapping][order].append('0');
                lastUpdateIndex[mapping][order]++;
                bitCount[mapping][order]++;
            }
            stringArray[mapping][order].append('1');
            lastUpdateIndex[mapping][order]++;
            bitCount[mapping][order]++;
        }

        else{ //preserve is false
            //finish previous chunk
            while (lastUpdateIndex[mapping][order]<kk-1 && inProgress[mapping][order]){
                stringArray[mapping][order].append('0');
                lastUpdateIndex[mapping][order]++;
                bitCount[mapping][order]++;
            }
            groupCount[mapping][order]++;

            //build current chunk
            //System.out.println("here");
            for (int i = 0; i<orderTempIndex[order]; i++){
                stringArray[mapping][order].append('0');
                bitCount[mapping][order]++;
            }
            stringArray[mapping][order].append('1');
            bitCount[mapping][order]++;


            lastUpdateIndex[mapping][order] = orderTempIndex[order];

            //keep inProgress and preserve true
            if (orderTempIndex[order]<kk-1){
                inProgress[mapping][order] = true;
                preserve[mapping][order] = true;
            }
            if (order<maxOrder-1) {
                treeUpdate(order + 1, kk, mapping);
            }
        }

        if (orderTempIndex[order] == kk-1){
            inProgress[mapping][order] = false;
        }
    } // Tree Update

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

    static void cleanEnds(int kk){ //might not be needed
        for (int tempMap = 0; tempMap < mapDepth; tempMap++) {
            for (int tempOrder = 0; tempOrder < maxOrder; tempOrder++) {
                while (lastUpdateIndex[tempMap][tempOrder]<kk-1){
                    stringArray[tempMap][tempOrder].append('0');
                    lastUpdateIndex[tempMap][tempOrder]++;
                }
                groupCount[tempMap][tempOrder]++;
            }
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

    static void generate(){
        //generate random binary data in binArray
        presentCount = 0;
        boolean foundOne = false;
        for (int i=0; i<binArray.length;i++){
            double temp = Math.random();

            //manually add bit dense areas
        /*if (((i>data*.1)&&(i<data*.125))||((i>data*.475)&&(i<data*.525))||((i>data*.7)&&(i<data*.725))){
            probability = .3;
        }
        else {
            probability = .0007;
        }
         */

            if (temp<probability){
                rawData.append('1');
                binArray[i] = 1;
                presentCount++;
                if (!foundOne){
                    //firstOne = i;
                    foundOne = true;
                }
            }
            else{
                rawData.append('0');
            }
        }
        //System.out.println("Data Generated");
        //System.out.println("First at "+firstOne);
        //startTime = System.currentTimeMillis();

        //populate the stringArray array with worst case capacities
    } //Generate



}

//--------Feedback--------//
//Time analysis
//double endTime = System.currentTimeMillis();
//System.out.println("Finished in "+ (endTime - startTime)+"ms");
//System.out.println("Bits: " +presentCount);

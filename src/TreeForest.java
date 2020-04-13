import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class TreeForest extends TreeCompress{
    public static void compress(String inputFilePath,String outputFilePath) throws IOException{

        //Import Image to compress
        BufferedImage image;
        File input = new File(inputFilePath);
        image = ImageIO.read(input);
        int width = image.getWidth();
        int height = image.getHeight();

        data = width*height;

        //for each map, reverse bits if >50% are '1'
        reverseAnalysis(image,width,height);

        int k=8; //grouping of 8 bits

        //Initialize variables
        initVariables(k);

        //Scan over image/data and compress
        compressData(k, width, height, image);


        //Shrink trees and write to file
        File f = new File(outputFilePath);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        for (int tempMap = 0; tempMap < mapDepth; tempMap++){
            writeToFile(treeArray[tempMap].shrinkByteTree(data, width, height,reverse[tempMap]),tempMap,outputFilePath,out);
        }
        out.close();
    }

    public static void decompress(String inputFilePath,String outputFilePath) throws IOException{
        int width = 0,height=0;
        System.out.println(inputFilePath);
        FileInputStream fis = new FileInputStream(inputFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipInputStream zis = new ZipInputStream(bis);
        ZipEntry tempFile;

        int tempMap = 0;
        while ((tempFile = zis.getNextEntry()) != null) {
            FileInputStream currFile = new FileInputStream(inputFilePath + "\\" + tempFile.getName().substring(1)); //IOException <---------------
            BufferedInputStream currStream = new BufferedInputStream(currFile);

            int width1 = currStream.read();
            int width2 = currStream.read();
            int height1 = currStream.read();
            int height2 = currStream.read();
            width = ((width1<<8)&0b1111111)|width2; //Image width
            height = (height1<<8)|height2; //Image height

            data = width*height;
            reverse[tempMap] = ((width1>>>7)&0b1)==1; //Reverse bit
            initVariables(8);

            int tempOrder = 0;
            byte prevByte=1;
            while( currStream.available() > 0 ){
                byte currByte = (byte)currStream.read();
                if (currByte==0 && prevByte==0){
                    tempOrder++;
                }
                else if (currByte !=0){
                    treeArray[tempMap].setData(tempOrder,treeArray[tempMap].getOrderIndex(tempOrder));
                    treeArray[tempMap].incIndex(tempOrder);
                }

                prevByte = currByte;
            }
            tempMap++;
        }

        //Begin decompression
        System.out.println("Decompression start.");
        decompressData(8, width, height,outputFilePath);
        System.out.println("Decompression end.");

    }

    static void writeToFile(byte[] byteArr, int mapping, String outputPath,ZipOutputStream out) throws IOException{
        ZipEntry entry = new ZipEntry("/m_"+mapping+".bin");
        out.putNextEntry(entry);
        out.write(byteArr, 0, byteArr.length);
        out.closeEntry();
    }

    static void initVariables(int k){
        //initialize data
        maxOrder = (int)Math.ceil(Math.log(data)/Math.log(k))+1;

        //
        treeArray = new ByteTree[mapDepth];
        for (int tempMap = 0; tempMap<mapDepth; tempMap++){
            treeArray[tempMap] = new ByteTree(data,k,maxOrder);
        }

        buildOrderIndex = new int[maxOrder]; //[0] keeps current build index - [1] keeps last updated index(when order 0 data = 1)
        reconIndex = new int[mapDepth][maxOrder];

        orderTempIndex = new int[maxOrder];
        lastUpdateIndex = new int[mapDepth][maxOrder];
        preserve = new boolean[mapDepth][maxOrder];
        inProgress = new boolean[mapDepth][maxOrder];
        lastRun = false;
        //Counting Functions
        bitCount = new int[mapDepth][maxOrder];
        groupCount = new int[mapDepth][maxOrder];
        presentCount = 0;

    }
}

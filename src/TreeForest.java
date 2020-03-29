import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class TreeForest extends TreeCompress {

    public static void main (String[] args) throws IOException{
        //Import Image to compress
        BufferedImage image;
        File input = new File("INPUT_FILE_PATH");
        image = ImageIO.read(input);
        int width = image.getWidth();
        int height = image.getHeight();

        data = width*height; //(or arbitrary binary length for testing)

        //determine whether to map 1's or 0's for each map
        reverseAnalysis(image,width,height);

        int k=8;
        //Initialize variables
        initVariables(k);

        //Scan over image/data and compress
        long start = System.currentTimeMillis();
        compressData(k, width, height, image);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Image compressed in "+timeElapsed + "ms");

        //Shrink trees and write to file
        /*
        start = System.currentTimeMillis();
        for (int tempMap = 0; tempMap < mapDepth; tempMap++){
            writeToFile(treeArray[tempMap].shrinkTree(),tempMap);
        }
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println("Arrays decreased in " + timeElapsed + "ms");
         */

        //Decompress image directly from ByteTree objects
        start = System.currentTimeMillis();
        decompressData(k, width, height);
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println("Image decompressed in " + timeElapsed + "ms");
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

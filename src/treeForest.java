import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class treeForest extends TreeCompress {

    public static void main (String[] args) throws IOException{
        //Import Image to compress
        BufferedImage image;
        File input = new File("C:/Users/Sargy/IdeaProjects/Compression/resources/test.jpg");
        image = ImageIO.read(input);
        int width = image.getWidth();
        int height = image.getHeight();

        data = width*height; //(or arbitrary binary length for testing)

        //determine whether to map 1's or 0's for each map
        reverseAnalysis(image,width,height);

        //cycle through different grouping sizes and test the algorithm
        for (int k = 8; k<9; k+=4){
            //Initialize variables
            initVariables(k);

            //Scan over image/data and compress
            compressData(k, width, height, image);
            cleanEnds(k); //might not be needed

            //Results (for the entire dataset)
            printResults(k);

            decompressData(k, width, height);
            System.out.println("Complete.");
        }
    }

    static void initVariables(int k){
        //initialize data
        maxOrder = (int)Math.ceil(Math.log(data)/Math.log(k))+1;
        //stringArray = new StringBuilder[mapDepth][maxOrder];

        //
        treeArray = new ByteTree[mapDepth];
        for (int tempMap = 0; tempMap<mapDepth; tempMap++){
            treeArray[tempMap] = new ByteTree(data,k,maxOrder);
        }

        buildOrderIndex = new int[maxOrder]; //[0] keeps current build index - [1] keeps last updated index(when order 0 data = 1)

        orderTempIndex = new int[maxOrder];
        lastUpdateIndex = new int[mapDepth][maxOrder];
        preserve = new boolean[mapDepth][maxOrder];
        inProgress = new boolean[mapDepth][maxOrder];
        lastRun = false;
        //Counting Functions
        bitCount = new int[mapDepth][maxOrder];
        groupCount = new int[mapDepth][maxOrder];
        presentCount = 0;

        //initialize stringbuilders
        /*
        for (int tempMap = 0; tempMap < mapDepth; tempMap++) {
            for (int maxOrd = 0; maxOrd < maxOrder; maxOrd++) {
                stringArray[tempMap][maxOrd] = new StringBuilder(data / (int) Math.pow(k, maxOrd + 1));
            }
        }
         */
    }

    static void printResults(int k){
        int totalcount = 0;
        /*
        for (int i = 0; i< mapDepth; i++) {
            for (int j = 0; j < maxOrder-1 ; j++) {
                totalcount += stringArray[i][j].length();
                System.out.println(i + " " + stringArray[i][j]);
                //if ((k==16)){
                //    System.out.println(stringArray[i][j]);
                //}
            }
        }

        //Efficiency analysis
        System.out.println("[Groups of " + k + "]: " + "Bits used: " + totalcount + ", 'Actual': " + (presentCount * 8) + "  [ "+(((float)totalcount/(float)(presentCount*8))*100)+"% of original ]");
        System.out.println();
        int overhead = 0;

        for (int x = 0; x< mapDepth; x++){
            int tempCount = 0;
            for (int y = 0; y<maxOrder; y++){
                tempCount += stringArray[x][y].length();
            }
            //System.out.println("Map: "+ x + " | Bits: "+tempCount + " | Count: "+ hashCount[x] + " | Reverse: " + reverse[x]);
            //System.out.println("---------Base: "+stringArray[x][0].length());
            overhead+= (tempCount-stringArray[x][0].length());
        }
        System.out.println(overhead);

         */
    }
}

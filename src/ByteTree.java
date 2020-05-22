//This work is licensed under the Creative Commons
// Attribution-NonCommercial-NoDerivs 3.0 Unported License.
// To view a copy of this license, visit
// http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter
// to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.


// ---------------------------------- ByteTree ----------------------------------//
//Used to hold compressed data for writing to file or decompressing data
//
//Implemented using a Jagged array, with
//      the first dimension holding as many arrays as there are possible orders in the byte tree
//      the second dimension holding byte arrays, storing byte data at the respective order in the tree

public class ByteTree {

    private int tempMask = 0b10000000;
    private byte[][] byteTree;//jagged array used to implement the object
    private int[] writeIndex; //current index to write to
    private int[] readIndex;  //current index to read
    private int maxOrd;       //total orders in the tree (log base 8 of the total data size)

    //Initializer
    ByteTree(int dataSize, int k, int maxOrder){
        this.maxOrd = maxOrder;
        this.writeIndex = new int[this.maxOrd];
        this.byteTree = new byte[this.maxOrd][];
        this.readIndex = new int[this.maxOrd];

        //Initialize ByteTree array and fill it with empty bytes
        for (int tempOrder = 0; tempOrder<this.maxOrd; tempOrder++){

            int maxPossibleData = (int)Math.ceil(((double)dataSize / (Math.pow(k,tempOrder+1)))) + 1;
            this.byteTree[tempOrder] = new byte[maxPossibleData];

            for (int tempIndex = 0; tempIndex < maxPossibleData; tempIndex++ ){
                this.byteTree[tempOrder][tempIndex] = (byte) 0b00000000;
            }
        }
    }

    //Sets relevant bit of corresponding byte to high
    void setData(int order, int bitIndex){
        int currentIndex = this.getWriteIndex(order);
        this.byteTree[order][currentIndex] = (byte) (this.byteTree[order][currentIndex]|(tempMask>>>bitIndex));
    }

    //Sets byte to passed argument
    void setData(int order, byte byteData){
        int currentIndex = this.getWriteIndex(order);
        this.byteTree[order][currentIndex] = byteData;
    }

    //Returns byte at corresponding order,index
    byte getData(int order, int index){
        return this.byteTree[order][index];
    }

    //Auto-reader, returns byte at readIndex[order]
    byte getData(int order){
        //System.out.println(order + " " + readIndex[order]);
        byte data = this.byteTree[order][readIndex[order]];
        this.incReadIndex(order);
        return data;
    }

    //Increase write index
    void incWriteIndex(int order){
        this.writeIndex[order]++;
    }

    //Decrease write index
    void decWriteIndex(int order) { this.writeIndex[order]--; }

    //Increase read index
    void incReadIndex(int order){
        this.readIndex[order]++;
    }

    //Decrease read index
    void decReadIndex(int order) { this.readIndex[order]--; }

    //Returns the index of the current byte to be written
    int getWriteIndex(int order){
        return this.writeIndex[order];
    }

    //Return read index
    int getReadIndex(int order) { return this.readIndex[order]; }

    //Writes jagged 2D byteTree array to 1D array
    byte[] condenseByteTree(boolean reverse){
        this.patchWriteIndexes();

        int condensedDataSize = 0;
        for (int tempOrder = 0; tempOrder < this.maxOrd; tempOrder++){
            condensedDataSize += this.getWriteIndex(tempOrder)+2;
        }

        condensedDataSize+=4;

        byte[] condensedByteTree = new byte[condensedDataSize];

        //write condensedDataSize (length of array) tag to array
        condensedByteTree[0]=(byte)((condensedDataSize>>24)&0xff);
        condensedByteTree[1]=(byte)((condensedDataSize>>16)&0xff);
        condensedByteTree[2]=(byte)((condensedDataSize>>8)&0xff);
        condensedByteTree[3]=(byte)((condensedDataSize)&0xff);
        if (reverse){ //Change reverse-bit to a 1
            condensedByteTree[0]|=0b10000000;
        }

        //Scan byteTree and build condensedByteTree array with data, up to (write index + 1)
        int buildIndex = 4;
        for (int tempOrder = 0; tempOrder < this.maxOrd; tempOrder++){
            for (int tempIndex = 0; tempIndex <= this.getWriteIndex(tempOrder)+1; tempIndex++){
                condensedByteTree[buildIndex] = this.getData(tempOrder,tempIndex);
                buildIndex++;
            }
            condensedDataSize += this.getWriteIndex(tempOrder);
        }

        return condensedByteTree;
    }

    //Return size of the condensedArray
    int getCondensedSize(){
        int size = 0;
        for (int tempOrder = 0; tempOrder < this.maxOrd; tempOrder++){
            size += this.getWriteIndex(tempOrder)+1;
        }
        return size;
    }

    //Turn byte array into a byteTree object
    void toByteTree(byte[] byteArray, boolean inverted){
        byte currentByte;
        int currentOrder = 0;

        //Scan byte array
        for (int index = 0; index < byteArray.length; index++){
            currentByte = byteArray[index];

            //If data is live, set the byte tree at the given order to the current byte
            if(currentByte!=0 && (this.getWriteIndex(currentOrder)!=byteTree[currentOrder].length)){
                this.setData(currentOrder,currentByte);
                this.incWriteIndex(currentOrder);
            } else if (currentOrder < maxOrd - 1) { currentOrder++; }
        }
        this.patchWriteIndexes();
    }

    //If the write index is hovering over a byte equal to 0, decrease its value by 1
    void patchWriteIndexes() { //called when all data is written, increases writeIndex if current byte isn't = 0
        for (int tempOrder = 0; tempOrder < this.maxOrd; tempOrder++){
            if (this.getWriteIndex(tempOrder)<byteTree[tempOrder].length && this.getData(tempOrder,this.getWriteIndex(tempOrder)) == 0){
                this.decWriteIndex(tempOrder);
            }
        }
    }
}
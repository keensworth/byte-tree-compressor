public class ByteTree {
    //Used to hold compressed data for writing to file or decompressing data

    int tempMask = 0b10000000;
    private byte[][] byteTree;
    private int[] writeIndex; //current index to write to
    private int[] readIndex;
    private int maxOrd;

    ByteTree(int dataSize, int k, int maxOrder){
        this.maxOrd = maxOrder;
        this.writeIndex = new int[this.maxOrd];
        this.byteTree = new byte[this.maxOrd][];

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

    byte getData(int order){
        byte data = this.byteTree[order][readIndex[order]];
        this.incReadIndex(order);
        return data;
    }

    void incWriteIndex(int order){
        this.writeIndex[order]++;
    }

    void decWriteIndex(int order) { this.writeIndex[order]--; }

    void incReadIndex(int order){
        this.readIndex[order]++;
    }

    void decReadIndex(int order) { this.readIndex[order]--; }

    //Returns the index of the current byte to be written
    int getWriteIndex(int order){
        return this.writeIndex[order];
    }

    int getReadIndex(int order) { return this.readIndex[order]; }

    byte[] condenseByteTree(boolean reverse){
        this.patchWriteIndexes();

        int condensedDataSize = 0;
        for (int tempOrder = 0; tempOrder < this.maxOrd; tempOrder++){
            condensedDataSize += this.getWriteIndex(tempOrder)+2;
        }

        condensedDataSize+=4;

        byte[] condensedByteTree = new byte[condensedDataSize];

        //write condensedDataSize tag
        condensedByteTree[0]=(byte)((condensedDataSize>>24)&0xff);
        condensedByteTree[1]=(byte)((condensedDataSize>>16)&0xff);
        condensedByteTree[2]=(byte)((condensedDataSize>>8)&0xff);
        condensedByteTree[3]=(byte)((condensedDataSize)&0xff);
        if (reverse){ //Change reverse-bit to a 1
            condensedByteTree[0]|=0b10000000;
        }
        
        int buildIndex = 4;
        for (int tempOrder = 0; tempOrder < this.maxOrd; tempOrder++){
            for (int tempIndex = 0; tempIndex <= this.getWriteIndex(tempOrder); tempIndex++){
                condensedByteTree[buildIndex] = this.getData(tempOrder,tempIndex);
                buildIndex++;
            }
            condensedDataSize += this.getWriteIndex(tempOrder);
        }

        return condensedByteTree;
    }

    void toByteTree(byte[] byteArray, boolean inverted){
        int currentOrder = 0;
        byte currentByte;
        for (int index = 0; index < byteArray.length; index++){
            currentByte = byteArray[index];

            if (inverted) {
                currentByte ^= 0xff;
            }

            if(currentByte!=0){
                this.setData(currentOrder,currentByte);
                this.incWriteIndex(currentOrder);
            } else {
                currentOrder++;
            }
        }
        this.patchWriteIndexes();
    }

    void patchWriteIndexes() { //called when all data is written, increases writeIndex if current byte isn't = 0
        for (int tempOrder = 0; tempOrder < this.maxOrd; tempOrder++){
            if (this.getData(tempOrder,this.getWriteIndex(tempOrder)) == 0){
                this.decWriteIndex(tempOrder);
            }
        }
    }
}
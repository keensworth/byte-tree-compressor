public class ByteTree {
    //Used to hold compressed data for writing to file or decompressing

    int tempMask = 0b10000000;
    private byte[] byteTree;
    private int[] baseIndexArray; //starting index for each order
    private int[] lastUpdate; //index of most recent setData total index (to know how much to consider when writing to disk)
    private int[] orderCount; //order-relative index of last update
    private int dataLength; //total array length

    private int maxOrd, width, height;
    ByteTree(int data, int k, int maxOrder){
        dataLength = data;
        //Init index array to navigate tree array
        this.baseIndexArray = new int[maxOrder];
        this.lastUpdate = new int[maxOrder];;
        this.orderCount = new int[maxOrder];
        this.maxOrd = maxOrder;

        this.baseIndexArray[0]=0;
        for (int i=1; i<maxOrder; i++){
            this.baseIndexArray[i] = (int)Math.ceil(dataLength/Math.pow(8,i)) + this.baseIndexArray[i-1];
        }

        int length = (dataLength/(k-1))+maxOrder;
        this.byteTree = new byte[length];
        for (int j = 0; j<length; j++){
            this.byteTree[j] = (byte) 0b00000000;
        }
    }

    //Sets relevant bit of corresponding byte to high
    void setData(int order, int dataIndex){
        int tempIndex = this.baseIndexArray[order]+this.getOrderIndex(order);
        this.lastUpdate[order] = tempIndex;
        this.byteTree[tempIndex] = (byte) (this.byteTree[tempIndex]|(tempMask>>>dataIndex));
    }

    void incIndex(int order){
        this.orderCount[order]++;
    }

    void decIndex(int order) { this.orderCount[order]--; }

    //Returns the index of the most recently edited byte, relative to order
    int getOrderIndex(int order){
        return this.orderCount[order];
    }

    //Returns byte at corresponding index
    byte getData(int order, int index){
        return this.byteTree[this.baseIndexArray[order]+index];
    }

    byte[] getByteTree(){
        return this.byteTree;
    }

    byte[] shrinkByteTree(int dataLength, int width, int height, boolean reverse){
        int[] newBaseIndexArray = new int[this.maxOrd];
        newBaseIndexArray[0]=0;
        int dataCount = 0;

        for (int ord = 0; ord < this.maxOrd; ord++){
            int peakIndex = (int)Math.ceil(dataLength/Math.pow(8,ord));

            int usefulData = this.getOrderIndex(ord);
            if (usefulData == peakIndex){
                usefulData--;
                this.decIndex(ord);
            }
            dataCount += this.getOrderIndex(ord) + 1;
            if (ord>0) {
                newBaseIndexArray[ord] = this.getOrderIndex(ord-1) + 2 + newBaseIndexArray[ord-1];
            }
        }
        dataCount+=4; //2 bytes for width tag and 2 bytes for height tag
        byte[] newArray = new byte[dataCount+this.maxOrd];
        System.out.println(dataCount);

        //create width/height tags
        newArray[0]=(byte)(width>>>8);
        newArray[1]=(byte)(width&0b11111111);
        newArray[2]=(byte)(height>>>8);
        newArray[3]=(byte)(height&0b11111111);

        if (reverse){ //Change reverse-bit to a 1
            newArray[0]|=0b10000000;
        }

        //Remove empty bytes from bytetree array
        for (int ord = 0; ord < this.maxOrd; ord++){
            int maxOrderIndex = this.getOrderIndex(ord);

            for (int index = 0; index < maxOrderIndex+1; index++){
                int totalIndex = newBaseIndexArray[ord]+index+4;

                if (index != maxOrderIndex) { newArray[totalIndex] = this.getData(ord,index); } //insert data
                else{ newArray[totalIndex] = 0; } //add 0-flag (to indicate order change in byte stream)
            }
        }
        return newArray;
    }
}
public class ByteTree {
    int tempMask = 0b10000000;
    private byte[] byteTree;
    private int[] indexArray;
    private int[] lastUpdate; //index of most recent setData total index (to know how much to consider when writing to disk)
    private int[] orderCount;

    private int maxOrd, width, height;
    ByteTree(int dataLength, int k, int maxOrder){
        //Init index array to navigate tree array
        this.indexArray = new int[maxOrder];
        this.lastUpdate = new int[maxOrder];;
        this.orderCount = new int[maxOrder];
        this.maxOrd = maxOrder;

        this.indexArray[0]=0;
        for (int i=1; i<maxOrder; i++){
            this.indexArray[i] = (int)Math.ceil(dataLength/Math.pow(8,i)) + this.indexArray[i-1];
        }

        int length = (dataLength/(k-1))+maxOrder;
        this.byteTree = new byte[length];
        for (int j = 0; j<length; j++){
            this.byteTree[j] = (byte) 0b00000000;
        }
    }

    //Sets relevant bit of corresponding byte to high
    void setData(int order, int dataIndex){
        int tempIndex = this.indexArray[order]+this.getOrderIndex(order);
        this.lastUpdate[order] = tempIndex;
        this.byteTree[tempIndex] = (byte) (this.byteTree[tempIndex]|(tempMask>>>dataIndex));
    }

    void incIndex(int order){
        this.orderCount[order]++;
    }

    private int getOrderIndex(int order){
        return this.orderCount[order];
    }

    //Returns byte at corresponding index
    byte getData(int order, int index){
        return this.byteTree[this.indexArray[order]+index];
    }

    byte[] getByteTree(){
        return this.byteTree;
    }

    byte[] shrinkTree(){
        int dataCount = 0;
        for (int ord = 0; ord < this.maxOrd; ord++){
            int usefulData = this.getOrderIndex(ord);
            if (usefulData != 0) { dataCount += this.getOrderIndex(ord) + 1; }
        }
        byte[] newArray = new byte[dataCount];
        System.out.println(dataCount);


        //remove empty bytes
        for (int ord = 0; ord < this.maxOrd; ord++){
            int maxOrderIndex = this.getOrderIndex(ord);

            for (int index = 0; index <= maxOrderIndex+1; index++){
                int baseIndex;

                if (ord==0){ baseIndex = 0; }
                else {baseIndex = this.getOrderIndex(ord-1)+1;}

                int totalIndex = baseIndex+index;

                if (index != maxOrderIndex+1) { newArray[totalIndex] = this.getData(ord,index); } //insert data
                else{ newArray[totalIndex] = 0; } //add 0-flag (to indicate order change when reading array)
            }
        }
        return newArray;
    }
}

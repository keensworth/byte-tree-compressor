public class ByteTree {
    private byte[] byteTree;
    private int[] indexArray;
    ByteTree(int dataLength, int k, int maxOrder){
        //Init index array to navigate tree array
        indexArray = new int[maxOrder];
        for (int i=1; i<maxOrder; i++){
            indexArray[i] = (int)Math.ceil(dataLength/Math.pow(8,i));
        }

        int length = dataLength/(k-1);
        byteTree = new byte[length+1];
    }

    public byte getData(int order, int index){
        return this.byteTree[indexArray[order]+index];
    }

    public void appendData(int order, int index, int dataIndex){
        int tempIndex = indexArray[order]+index;
        int tempMask = 0b10000000;
        this.byteTree[tempIndex] |= (tempMask>>>dataIndex);
    }
}

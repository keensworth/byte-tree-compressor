public class Utility {
    public static ByteTree[] initByteForest(int treeCount, int dataSize, int orders){
        ByteTree[] byteTreeArray = new ByteTree[treeCount];
        for (int byteTree = 0; byteTree<treeCount; byteTree++){
            byteTreeArray[byteTree] = new ByteTree(dataSize,8,orders);
        }
        return byteTreeArray;
    }
}

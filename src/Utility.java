//This work is licensed under the Creative Commons
// Attribution-NonCommercial-NoDerivs 3.0 Unported License.
// To view a copy of this license, visit
// http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter
// to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.

public class Utility {
    public static ByteTree[] initByteForest(int treeCount, int dataSize, int orders){
        ByteTree[] byteTreeArray = new ByteTree[treeCount];
        for (int byteTree = 0; byteTree<treeCount; byteTree++){
            byteTreeArray[byteTree] = new ByteTree(dataSize,8,orders);
        }
        return byteTreeArray;
    }
}

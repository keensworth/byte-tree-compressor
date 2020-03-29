public class ByteForest{
    int width, height;
    ByteTree[] treeArray;

    ByteForest(ByteTree[] byteTreeArr){
        this.treeArray = byteTreeArr;
    }

    //work with specific tree
    ByteTree tree(int map){
        return this.treeArray[map];
    }

    void setSize(int width, int height){
        this.width = width;
        this.height = height;
    }
}

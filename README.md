## Byte Tree Compressor

---

### About

This project prototypes a lossless image compression technique, described below. It is important to note that the program accepts PNG and JPG images, and calculated compression ratios are made assuming the amount of data in an image is (Width * Height * 3 byes/pixel). Being that PNG and JPG are already compressed formats, actual compression (relative to the input file's actual size) is unlikely. The sole purpose of this project is to verify a reduction in the size of data assuming the input was a raw image, and successful lossless decompression of said data.

### Building

#### IntelliJ

1. Copy https://github.com/keensworth/byte-tree-compressor.git 
2. Go to **File** > **New** > **Project from Version Control...** > **Git**
3. Paste link into the URL textbox and git **Clone**
4. Build project
5. Run **WindowMain**


### The Compression / Decompression Process

The concept of this technique is to build a 8:1 tree above the color bitmaps (24 total, 8 for Red, 8 for Green, 8 for Blue). 
The process involves scanning over each bitmap byte by byte. Each byte has a node (1 bit) above it, and that node is equal to:

- 0 if the byte is equal to 0
- 1 otherwise

This process is repeated recursively, grouping those nodes into bytes and creating new nodes above those bytes. 
At the end of the process, a 0 at any point in the tree (excluding the base, raw data) indicates that all bytes and sub-bytes are equal to 0.

Any zero-byte (and sub-bytes) do not need to be kept when the tree is written to file, as the tree itself can self-address the non-zero base bytes.

The decompression process rebuilds the original bitmaps from the trees, and combines these bitmaps to form the original image.


### ToDo
- Parallel de/compression
- Batch de/compression
- Text (UTF-8) de/compression
- Alpha channel compatibility
- Greater file-type compatibility
- Add .bt file tags to rebuild image into original file-type
- Eyefriendly UI

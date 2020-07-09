
## Class file

<https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html>

class file consists of a stream of 8-bit bytes. All 16-bit, 32-bit, and 64-bit quantities are constructed by reading in two, four, and eight consecutive 8-bit bytes

Class 文件即为大端对齐的字节流

A class file consists of a single ClassFile structure:

```
ClassFile {
    u4             magic;
    u2             minor_version;
    u2             major_version;
    u2             constant_pool_count;
    cp_info        constant_pool[constant_pool_count-1];
    u2             access_flags;
    u2             this_class;
    u2             super_class;
    u2             interfaces_count;
    u2             interfaces[interfaces_count];
    u2             fields_count;
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
```

u2 u4 代表2个字节和4个字节的无符号整数。
Tables 以 `_info` 结尾，是和 C 数组类似的数据结构。 

以 [TestClass.java](./TestClass.java) 为例(因为 class 文件以大端顺序序列化，需要使用 hexdump 的 -C 参数来忽略机器的大小端顺序按字节显示)

```
hexdump -C TestClass.class 
00000000  ca fe ba be 00 00 00 34  00 13 0a 00 04 00 0f 09  |.......4........|
00000010  00 03 00 10 07 00 11 07  00 12 01 00 01 6d 01 00  |.............m..|
00000020  01 49 01 00 06 3c 69 6e  69 74 3e 01 00 03 28 29  |.I...<init>...()|
00000030  56 01 00 04 43 6f 64 65  01 00 0f 4c 69 6e 65 4e  |V...Code...LineN|
00000040  75 6d 62 65 72 54 61 62  6c 65 01 00 03 69 6e 63  |umberTable...inc|
00000050  01 00 03 28 29 49 01 00  0a 53 6f 75 72 63 65 46  |...()I...SourceF|
00000060  69 6c 65 01 00 0e 54 65  73 74 43 6c 61 73 73 2e  |ile...TestClass.|
00000070  6a 61 76 61 0c 00 07 00  08 0c 00 05 00 06 01 00  |java............|
00000080  0f 54 6f 6f 6c 73 2f 54  65 73 74 43 6c 61 73 73  |.Tools/TestClass|
00000090  01 00 10 6a 61 76 61 2f  6c 61 6e 67 2f 4f 62 6a  |...java/lang/Obj|
000000a0  65 63 74 00 21 00 03 00  04 00 00 00 01 00 02 00  |ect.!...........|
000000b0  05 00 06 00 00 00 02 00  01 00 07 00 08 00 01 00  |................|
000000c0  09 00 00 00 1d 00 01 00  01 00 00 00 05 2a b7 00  |.............*..|
000000d0  01 b1 00 00 00 01 00 0a  00 00 00 06 00 01 00 00  |................|
000000e0  00 03 00 01 00 0b 00 0c  00 01 00 09 00 00 00 24  |...............$|
000000f0  00 03 00 01 00 00 00 0c  2a 2a b4 00 02 04 60 5a  |........**....`Z|
00000100  b5 00 02 ac 00 00 00 01  00 0a 00 00 00 06 00 01  |................|
00000110  00 00 00 06 00 01 00 0d  00 00 00 02 00 0e        |..............|
0000011e
```

开头的4个字节为 `0xCAFEBABE` 即 JVM Class 文件的 magic number，接下来两个字节为子版本号(0)，然后是主版本号0x34(十进制52，JDK1.8)


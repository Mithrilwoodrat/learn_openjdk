
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
package Runtime;

public class TestClass {
    private int m;
    private final int n = 1;
    private static long l = 2l;
    private String s = "test";
    public int inc () {
        return m = m +1;
    }
}
```

```
$ hexdump -C TestClass.class 
00000000  ca fe ba be 00 00 00 34  00 25 0a 00 0a 00 1d 09  |.......4.%......|
00000010  00 09 00 1e 08 00 1f 09  00 09 00 20 09 00 09 00  |........... ....|
00000020  21 05 00 00 00 00 00 00  00 02 09 00 09 00 22 07  |!.............".|
00000030  00 23 07 00 24 01 00 01  6d 01 00 01 49 01 00 01  |.#..$...m...I...|
00000040  6e 01 00 0d 43 6f 6e 73  74 61 6e 74 56 61 6c 75  |n...ConstantValu|
00000050  65 03 00 00 00 01 01 00  01 6c 01 00 01 4a 01 00  |e........l...J..|
00000060  01 73 01 00 12 4c 6a 61  76 61 2f 6c 61 6e 67 2f  |.s...Ljava/lang/|
00000070  53 74 72 69 6e 67 3b 01  00 06 3c 69 6e 69 74 3e  |String;...<init>|
00000080  01 00 03 28 29 56 01 00  04 43 6f 64 65 01 00 0f  |...()V...Code...|
00000090  4c 69 6e 65 4e 75 6d 62  65 72 54 61 62 6c 65 01  |LineNumberTable.|
000000a0  00 03 69 6e 63 01 00 03  28 29 49 01 00 08 3c 63  |..inc...()I...<c|
000000b0  6c 69 6e 69 74 3e 01 00  0a 53 6f 75 72 63 65 46  |linit>...SourceF|
000000c0  69 6c 65 01 00 0e 54 65  73 74 43 6c 61 73 73 2e  |ile...TestClass.|
000000d0  6a 61 76 61 0c 00 14 00  15 0c 00 0d 00 0c 01 00  |java............|
000000e0  04 74 65 73 74 0c 00 12  00 13 0c 00 0b 00 0c 0c  |.test...........|
000000f0  00 10 00 11 01 00 11 52  75 6e 74 69 6d 65 2f 54  |.......Runtime/T|
00000100  65 73 74 43 6c 61 73 73  01 00 10 6a 61 76 61 2f  |estClass...java/|
00000110  6c 61 6e 67 2f 4f 62 6a  65 63 74 00 21 00 09 00  |lang/Object.!...|
00000120  0a 00 00 00 04 00 02 00  0b 00 0c 00 00 00 12 00  |................|
00000130  0d 00 0c 00 01 00 0e 00  00 00 02 00 0f 00 0a 00  |................|
00000140  10 00 11 00 00 00 02 00  12 00 13 00 00 00 03 00  |................|
00000150  01 00 14 00 15 00 01 00  16 00 00 00 30 00 02 00  |............0...|
00000160  01 00 00 00 10 2a b7 00  01 2a 04 b5 00 02 2a 12  |.....*...*....*.|
00000170  03 b5 00 04 b1 00 00 00  01 00 17 00 00 00 0e 00  |................|
00000180  03 00 00 00 03 00 04 00  05 00 09 00 07 00 01 00  |................|
00000190  18 00 19 00 01 00 16 00  00 00 24 00 03 00 01 00  |..........$.....|
000001a0  00 00 0c 2a 2a b4 00 05  04 60 5a b5 00 05 ac 00  |...**....`Z.....|
000001b0  00 00 01 00 17 00 00 00  06 00 01 00 00 00 09 00  |................|
000001c0  08 00 1a 00 15 00 01 00  16 00 00 00 1f 00 02 00  |................|
000001d0  00 00 00 00 07 14 00 06  b3 00 08 b1 00 00 00 01  |................|
000001e0  00 17 00 00 00 06 00 01  00 00 00 06 00 01 00 1b  |................|
000001f0  00 00 00 02 00 1c                                 |......|
000001f6
```

### Magic Number

开头的4个字节为 `0xCAFEBABE` 即 JVM Class 文件的 magic number

### Version Number

接下来两个字节为子版本号(0)，然后是主版本号0x34(十进制52，JDK1.8)

```
minor version: 0
major version: 52
```

### 常量池 Constant Pool

The constant_pool is a table of structures (§4.4) representing various string constants, class and interface names, field names, and other constants that are referred to within the ClassFile structure and its substructures. The format of each constant_pool table entry is indicated by its first "tag" byte.

常量池中存储的为常量值(声明为 final 的变量等)、类名、接口名、字段名等字符串常量以及相关的引用(完整的类名，方法名需要由多个字符串常量拼接而成)。

引用包括下面的几种

* 类和接口的全限定名(Fully Qualified Name) 如 `#9 = Class #35 // Runtime/TestClass`
* 字段的名称和描述(Desciptrior) 如 `#2 = Fieldref #9.#30 Runtime/TestClass.n:I` 和
 `#4 = Fieldref #9.#32 //Runtime/TestClass.s:Ljava/lang/String;`
* 方法的名称和描述符 如 `#1 = Methodref #10.#29 // java/lang/Object."<init>":()V`

常量池前为 `constant_pool_count`，其值为常量池中值的数量+1，上面的例子中为37，即有36个常量。

例子中一共有 `m n l s` 四个变量和 `1 2l "test"` 3个常量，加上类名远远小于 36，原因是方法名和字段名需要由多个字符串常量拼接而成。

使用 `javap -v` 命令可以查看编译后 class 文件的常量池

```
$ javap -v TestClass.class
...
Constant pool:
   #1 = Methodref          #10.#29        // java/lang/Object."<init>":()V
   #2 = Fieldref           #9.#30         // Runtime/TestClass.n:I
   #3 = String             #31            // test
   #4 = Fieldref           #9.#32         // Runtime/TestClass.s:Ljava/lang/String;
   #5 = Fieldref           #9.#33         // Runtime/TestClass.m:I
   #6 = Long               2l
   #8 = Fieldref           #9.#34         // Runtime/TestClass.l:J
   #9 = Class              #35            // Runtime/TestClass
  #10 = Class              #36            // java/lang/Object
  #11 = Utf8               m
  #12 = Utf8               I
  #13 = Utf8               n
  #14 = Utf8               ConstantValue
  #15 = Integer            1
  #16 = Utf8               l
  #17 = Utf8               J
  #18 = Utf8               s
  #19 = Utf8               Ljava/lang/String;
  #20 = Utf8               <init>
  #21 = Utf8               ()V
  #22 = Utf8               Code
  #23 = Utf8               LineNumberTable
  #24 = Utf8               inc
  #25 = Utf8               ()I
  #26 = Utf8               <clinit>
  #27 = Utf8               SourceFile
  #28 = Utf8               TestClass.java
  #29 = NameAndType        #20:#21        // "<init>":()V
  #30 = NameAndType        #13:#12        // n:I
  #31 = Utf8               test
  #32 = NameAndType        #18:#19        // s:Ljava/lang/String;
  #33 = NameAndType        #11:#12        // m:I
  #34 = NameAndType        #16:#17        // l:J
  #35 = Utf8               Runtime/TestClass
  #36 = Utf8               java/lang/Object
```

Java Virtual Machine instructions do not rely on the run-time layout of classes, interfaces, class instances, or arrays. Instead, instructions refer to symbolic information in the constant_pool table.

JVM 字节码直接依赖常量池中的信息，而非运行时 class 等内存布局。

`cp_info        constant_pool[constant_pool_count-1];` 常量池中每个元素的结构都为下面的 `cp_info`

```
cp_info {
    u1 tag;
    u1 info[];
}
```

由第一个字节的 tag 字段表示该元素的类型

```
Constant Type	Value
CONSTANT_Class	7
CONSTANT_Fieldref	9
CONSTANT_Methodref	10
CONSTANT_InterfaceMethodref	11
CONSTANT_String	8
CONSTANT_Integer	3
CONSTANT_Float	4
CONSTANT_Long	5
CONSTANT_Double	6
CONSTANT_NameAndType	12
CONSTANT_Utf8	1
CONSTANT_MethodHandle	15
CONSTANT_MethodType	16
CONSTANT_InvokeDynamic	18
```

`cp_info tag` 字节后必须跟上描述对应 CONSTANT 结构信息的字段。因为每种 CONSTANT INFO 长度都不固定，人工解析太麻烦，就以第一个的 `Methodref` 为例，接下来的2个字节为类名在常量池中的索引位置。

```
CONSTANT_Methodref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}
```

以上面 TestClass 的字节码为例，对应 `CONSTANT_Methodref_info` 的字节码为 `0A 00 0A 00 1D`，显示为 `#1 = Methodref #10.#29`

第一个字节为十进制的 10， 标识这是一个 `CONSTANT_Methodref` 结构。
所以接下来的2个字节为方法所属的类常量的索引，在这里为 10，在 TestClass 的常量池为 `#10 = Class  #36` 又指向了索引为 36 的值

```
00000100:                         01 00 10 6A 61 76 61 2F            ...java/
00000110: 6C 61 6E 67 2F 4F 62 6A 65 63 74 00 21 00 09 00    lang/Object.!...
```

其结构为

```
CONSTANT_Utf8_info {
    u1 tag;
    u2 length;
    u1 bytes[length];
}
```

第一个字节 0X01 为其类型标识，接下来两个字节`0x10`为字符串长度，这里因为都是ascii字符所以其长度为16(根据 utf-8 的 [wiki](https://en.wikipedia.org/wiki/UTF-8), The first 128 characters (US-ASCII) need one byte )

接下来的两个字节 `001D` 为 `NameAndType` 的索引，对应的值为 `#29 = NameAndType #20:#21` 而索引 20 21 的值为 

```
#20 = Utf8               <init>
#21 = Utf8               ()V
```

拼接起来刚好为 `java/lang/Object."<init>":()V`。

### Access Flags

The value of the access_flags item is a mask of flags used to denote access permissions to and properties of this class or interface.  

`access_flags` 的值代表了类或接口的访问信息，如 Class 为类还是接口，是否为 public、是否定义为 abstract、是否被声明为 final 等。

使用 `javap -v` 可以查看 class 文件的 access_flags

```
javap -v Runtime/TestClass.class
  flags: ACC_PUBLIC, ACC_SUPER
```

```
Flag Name	Value	Interpretation
ACC_PUBLIC	0x0001	Declared public; may be accessed from outside its package.
ACC_FINAL	0x0010	Declared final; no subclasses allowed.
ACC_SUPER	0x0020	Treat superclass methods specially when invoked by the invokespecial instruction.
ACC_INTERFACE	0x0200	Is an interface, not a class.
ACC_ABSTRACT	0x0400	Declared abstract; must not be instantiated.
ACC_SYNTHETIC	0x1000	Declared synthetic; not present in the source code.
ACC_ANNOTATION	0x2000	Declared as an annotation type.
ACC_ENUM	0x4000	Declared as an enum type.
```

上面的 TestClass class 文件中对应 `access_flags` 的值为 `0x21`,  即 `0x20 & 0x01` ，对应 ACC_PUBLIC 和 ACC_SUPER。

The ACC_SUPER flag indicates which of two alternative semantics is to be expressed by the invokespecial instruction (§invokespecial) if it appears in this class. Compilers to the instruction set of the Java Virtual Machine should set the ACC_SUPER flag.
The ACC_SUPER flag exists for backward compatibility with code compiled by older compilers for the Java programming language. In Oracle’s JDK prior to release 1.0.2, the compiler generated ClassFile access_flags in which the flag now representing ACC_SUPER had no assigned meaning, and Oracle's Java Virtual Machine implementation ignored the flag if it was set.

ACC_SUPER 所有 JDK 1.0.2 之后版本的 class 文件该都必须设置该标志。


### this_class super_class & interfaces

class 文件中接下来的 `this_class` `super_class` `interfaces_count` `interfaces[]` 为类的索引、父类索引、以及接口的索引。

`this_class` 为常量池中指向当前类的索引值，对应的结构必须为 `CONSTANT_Class_info`。以 `TestClass.class` 为例，对应的值为 `00 09`，即索引为 9 的值，即 `#9 = Class #35 // Runtime/TestClass`。

`super_class` 同样为class文件中类的唯一父类对应的常量池的索引(Java 不允许多继承，所以最多只有一个父类)，如果没有父类则该值为0(只有 java.lang.Object没有父类)。同样以 `TestClass.class` 为例，其值为 `00 0A`，对应常量池中的值为 `#10 = Class #36 // java/lang/Object`

`interfaces_count` 字段描述该类实现了多少个接口。这些被实现的接口按 implements 语句的顺序从左到右排列(如果为接口则按 extends 语句从左到右排列)将对应常量池中的索引存放到 `interfaces[]` 中。

如[TestCLassimplements.java](./TestCLassimplements.java) 中，对应的 `interfaces_count` 为1，`interfaces[0]` 为`0x0B`对应 ` #11 = Class #38 // java/io/Serializable`。

而[TestCLass.java](./TestCLass.java) 中没有实现任何接口，所以其 `interfaces_count` 为0，`interfaces`为空不占空间。

### fields_count & fields[] 字段表

字段表用于描述类或者接口中的字段信息。

使用 `javap -v -p` 可以查看类中的所有字段

例如 [TestCLass.java](./TestCLass.java) 中的字段查看如下

```
  private int m;
    descriptor: I
    flags: ACC_PRIVATE

  private final int n;
    descriptor: I
    flags: ACC_PRIVATE, ACC_FINAL
    ConstantValue: int 1

  private static long l;
    descriptor: J
    flags: ACC_PRIVATE, ACC_STATIC

  private java.lang.String s;
    descriptor: Ljava/lang/String;
    flags: ACC_PRIVATE
```

在 class 文件中, `fields_count` 标记字段的数量。`field_info` 的结构如下

```
field_info {
    u2             access_flags;
    u2             name_index;
    u2             descriptor_index;
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
```


#### access_flags 作用域

包含字段的作用域 access_flags:

```
Flag Name	Value	Interpretation
ACC_PUBLIC	0x0001	Declared public; may be accessed from outside its package.
ACC_PRIVATE	0x0002	Declared private; usable only within the defining class.
ACC_PROTECTED	0x0004	Declared protected; may be accessed within subclasses.
ACC_STATIC	0x0008	Declared static.
ACC_FINAL	0x0010	Declared final; never directly assigned to after object construction (JLS §17.5).
ACC_VOLATILE	0x0040	Declared volatile; cannot be cached.
ACC_TRANSIENT	0x0080	Declared transient; not written or read by a persistent object manager. 不会被序列化
ACC_SYNTHETIC	0x1000	Declared synthetic; not present in the source code.
ACC_ENUM	0x4000	Declared as an element of an enum.
```

#### name_index 字段名

`name_index` 为字段简单名称在常量池中的索引，如 `private int m` 对应的值为 `0xB`，对应的常量为`m`。格式参考 [Unqualified Names](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2.2)，不能包含` . ; [ /`中的字符。
类方法名除了上述字符外，还不能包含`<>`(<init> <clinit> 两个方法除外)。(字段名和接口方法可以包含)

关于`<init>` 和 `<clinit>` 函数也可以参考 jvm 文档[2.9. Special Methods](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.9)，其中 `<init>` 是实例初始化时调用的函数，如 `Persion p = new Persion()` 时会调用。而 `<clinit>` 则是类初始化函数,类的 static 块会被转换为 class initialization 函数。

```
public class A {
    //<clinit>
    static{ System.out.println("Static Initializing...");}
}
```

更具体的可以参考[这篇文章](https://www.baeldung.com/jvm-init-clinit-methods)。

#### descriptor_index 描述符

字段类型在常量池中的索引 `descriptor_index`，例如 `private int m` 字段对应的的描述符索引在class文件中的偏移为`0x0000012A`，其值为 `0xC` 对应常量池中的 UTF-8 常量 `I`。

描述符的定义参考[文档](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.2)，用于描述字段的数据类型、方法的参数列表和返回值。

其遵循如下的语法

```
FieldDescriptor:
    FieldType

FieldType:
    BaseType
    ObjectType
    ArrayType

BaseType:
    B
    C
    D
    F
    I
    J
    S
    Z

ObjectType:
    L ClassName ;

ArrayType:
    [ ComponentType

ComponentType:
    FieldType
```

其中基础类型有下面几种 

```
BaseType Character	Type	Interpretation
B	byte	signed byte
C	char	Unicode character code point in the Basic Multilingual Plane, encoded with UTF-16
D	double	double-precision floating-point value
F	float	single-precision floating-point value
I	int	integer
J	long	long integer
L ClassName ;	reference	an instance of class ClassName
S	short	signed short
Z	boolean	true or false
[	reference	one array dimension
```



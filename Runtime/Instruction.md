指令

# Instruction Set Summary
[JVM 指令集(Instruction)](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.11) 由一个个字节码组成。字节码长度为 1 个字节，后面跟着零个或多个操作数。许多指令没有操作数，仅由一个操作码组成。

在不考虑异常处理的情况下，JVM 解释执行的流程大致等于下面的伪代码

```
do {
    atomically calculate pc and fetch opcode at pc;
    if (operands) fetch operands;
    execute the action for the opcode;
} while (there is more to do);
```

和 CPython 等解释性语言类似，一个while(1)循环，不停的获取 opcode 和 operands 并执行。

# Types and the Java Virtual Machine 指令类型

JVM 指令大多都包含其操作对应的数据类型信息。例如 `iload` 从局部变量中加载 int 型数据到栈中，而 fload 是加载浮点数。

大多数有类型相关字节码中
 * i 代表 int
 * l 代表 long
 * s 代表 short
 * b 代表 byte
 * c 代表 char
 * f 代表 float
 * d 代表 double
 * a 代表引用

因为 JVM 字节码只有 1 byte，所以只对特定的操作提供了有限的的类型相关指令。

 the instruction set is intentionally not orthogona 指令集不是完成独立的（非正交）。

JVM 的字节码类型映射表如下，可以看到 byte、short、char 都没有专门的 load store 等指令，也没有专门的 boolean 类型。这部分指令都是和 int 公用的。byte 和 short 会符号扩展(sign-extend)为 int，boolean 和 char 会零位扩充(zero-extend)为 init

```
|   opcode  	|   byte  	|  short  	|    int    	|   long  	|  float  	|  double 	|   char  	| reference 	|
|:---------:	|:-------:	|:-------:	|:---------:	|:-------:	|:-------:	|:-------:	|:-------:	|:---------:	|
| Tipush    	| bipush  	| sipush  	|           	|         	|         	|         	|         	|           	|
| Tconst    	|         	|         	| iconst    	| lconst  	| fconst  	| dconst  	|         	| aconst    	|
| Tload     	|         	|         	| iload     	| lload   	| fload   	| dload   	|         	| aload     	|
| Tstore    	|         	|         	| istore    	| lstore  	| fstore  	| dstore  	|         	| astore    	|
| Tinc      	|         	|         	| iinc      	|         	|         	|         	|         	|           	|
| Taload    	| baload  	| saload  	| iaload    	| laload  	| faload  	| daload  	| caload  	| aaload    	|
| Tastore   	| bastore 	| sastore 	| iastore   	| lastore 	| fastore 	| dastore 	| castore 	| aastore   	|
| Tadd      	|         	|         	| iadd      	| ladd    	| fadd    	| dadd    	|         	|           	|
| Tsub      	|         	|         	| isub      	| lsub    	| fsub    	| dsub    	|         	|           	|
| Tmul      	|         	|         	| imul      	| lmul    	| fmul    	| dmul    	|         	|           	|
| Tdiv      	|         	|         	| idiv      	| ldiv    	| fdiv    	| ddiv    	|         	|           	|
| Trem      	|         	|         	| irem      	| lrem    	| frem    	| drem    	|         	|           	|
| Tneg      	|         	|         	| ineg      	| lneg    	| fneg    	| dneg    	|         	|           	|
| Tshl      	|         	|         	| ishl      	| lshl    	|         	|         	|         	|           	|
| Tshr      	|         	|         	| ishr      	| lshr    	|         	|         	|         	|           	|
| Tushr     	|         	|         	| iushr     	| lushr   	|         	|         	|         	|           	|
| Tand      	|         	|         	| iand      	| land    	|         	|         	|         	|           	|
| Tor       	|         	|         	| ior       	| lor     	|         	|         	|         	|           	|
| Txor      	|         	|         	| ixor      	| lxor    	|         	|         	|         	|           	|
| i2T       	| i2b     	| i2s     	|           	| i2l     	| i2f     	| i2d     	|         	|           	|
| l2T       	|         	|         	| l2i       	|         	| l2f     	| l2d     	|         	|           	|
| f2T       	|         	|         	| f2i       	| f2l     	|         	| f2d     	|         	|           	|
| d2T       	|         	|         	| d2i       	| d2l     	| d2f     	|         	|         	|           	|
| Tcmp      	|         	|         	|           	| lcmp    	|         	|         	|         	|           	|
| Tcmpl     	|         	|         	|           	|         	| fcmpl   	| dcmpl   	|         	|           	|
| Tcmpg     	|         	|         	|           	|         	| fcmpg   	| dcmpg   	|         	|           	|
| if_TcmpOP 	|         	|         	| if_icmpOP 	|         	|         	|         	|         	| if_acmpOP 	|
| Treturn   	|         	|         	| ireturn   	| lreturn 	| freturn 	| dreturn 	|         	| areturn   	|
```

类型指令映射关系如下表

```
|  Actual type  	| Computational type 	| Category 	|
|:-------------:	|:------------------:	|:--------:	|
| boolean       	| int                	| 1        	|
| byte          	| int                	| 1        	|
| char          	| int                	| 1        	|
| short         	| int                	| 1        	|
| int           	| int                	| 1        	|
| float         	| float              	| 1        	|
| reference     	| reference          	| 1        	|
| returnAddress 	| returnAddress      	| 1        	|
| long          	| long               	| 2        	|
| double        	| double             	| 2         |
```

表格使用 [TablesGenerator.com](https://www.tablesgenerator.com/markdown_tables#) 生成。


# Load and Store Instructions 加载存储指令

* Load: `iload` `iload_<n>`(iload_1 = iload)
* Store: `istore, istore_<n>`
* Load Const
* 扩展访问的本地变量或者操作数 `wide`

数组操作也会转换为操作数栈上的操作。

# Arithmetic Instructions 算数指令

* Add: iadd, ladd, fadd, dadd.

* Subtract: isub, lsub, fsub, dsub.

* Multiply: imul, lmul, fmul, dmul.

* Divide: idiv, ldiv, fdiv, ddiv.

* Remainder: irem, lrem, frem, drem.

* Negate: ineg, lneg, fneg, dneg.

* Shift: ishl, ishr, iushr, lshl, lshr, lushr.

* Bitwise OR: ior, lor.

* Bitwise AND: iand, land.

* Bitwise exclusive OR: ixor, lxor.

* Local variable increment: iinc.

* Comparison: dcmpg, dcmpl, fcmpg, fcmpl, lcmp.


byte, short, char, boolean 都是和 int 共用指令。JVM 标准中并未规定溢出时应该如何处理。

JVM 算术指令只会在 div 和 rem 除0时抛出 ArithmeticException 异常。

浮点数运算遵守 IEEE 754 标准。

对 long 类型做比较时按有符号处理，而浮点数比较时会按 IEEE 754 做 nonsignaling comparisons。

# Type Conversion Instructions 类型转行指令

类型转换指令允许 JVM 数字类型之间的转换。用以弥补指令集正交性的不足。

向上扩展支持的类型如下：

* int to long, float, or double

* long to float or double

* float to double

对应的指令为 `i2l, i2f, i2d, l2f, l2d, and f2d`，即 int to long 等的简写。向上扩展整形数字不会丢失精度，浮点数向上扩展时若为 FP-strict(参考[文档](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.8.2)) 则不会丢失精度，非 FP-strict 模式则可能会丢失整体大小信息。

将 int、long 转换为 float，或者将 long 转行为 double 都可能会丢失精度。(丢失部分bit的数据，结果会按 IEEE 754 取整)

整数扩展不会导致 run-time exception。

byte、char、short 默认会扩展位 int，不需要指令显示扩展。


JVM 也支持以下的向下转型：

* int to byte, short, or char

* long to int

* float to int or long

* double to int, long, or float

向下转型的指令为 ` i2b, i2c, i2s, l2i, f2i, f2l, d2i, d2l,  d2f`。向下转型可能会得到不同大小、不同符号的结果，以及可能损失精度。 int 和 long 向下转型的时候只是简单的丢弃后N的bit。(JVM 整数是有符号以补码形式存储，丢弃一些bit后可能会导致符号变化)

浮点数转为整数( int or long)时：
 * 若浮点数值为 NaN，则转换后整数值为0.
 * 若浮点数的值不是无限循环的，浮点数会按 IEEE 754 取整。
 * 若值太小，则会转换为 int、long 的最小值(如 INTMAX)
 * 若值太大，则会转换为 int、long 的最大值

double 转换为 float 也遵守 IEEE 754 标准。

无论什么情况，数值类型转换指令不会造成 RunTime Exception（不要和 IEEE 754 浮点数异常混淆，参考[文档](https://docs.oracle.com/cd/E19957-01/806-3568/ncg_handle.html)）


# Object Creation and Manipulation 对象创建与操作

对象实例(class instance)和数组都属于 objects，但在 JVM 中他们有不同的操作指令。

* Create a new class instance: `new`.

* Create a new array: `newarray`, `anewarray`, `multianewarray`.

* Access fields of classes (static fields, known as class variables) and fields of class instances (non-static fields, known as instance variables): `getfield`, `putfield`, `getstatic`, `putstatic`.

* 从数组加载到操作数栈 Load an array component onto the operand stack: `baload, caload, saload, iaload, laload, faload, daload, aaload`.

* Store a value from the operand stack as an array component: bastore, castore, sastore, iastore, lastore, fastore, dastore, aastore.

* Get the length of array: arraylength.

* Check properties of class instances or arrays: instanceof, checkcast.

# Operand Stack Management Instructions 操作数栈管理

* 出栈 pop, pop2, 
* 复制栈顶 `dup, dup2, dup_x1, dup2_x1, dup_x2, dup2_x2` 。参考[文档](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.dup)
* 交换 `swap` , swap the top two operand stack values






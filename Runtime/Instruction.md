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



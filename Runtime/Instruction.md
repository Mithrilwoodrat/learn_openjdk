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


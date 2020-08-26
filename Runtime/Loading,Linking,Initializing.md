Loading, Linking, and Initializing

<https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html>

# JVM 类加载机制

JVM 动态加载(loads)、链接(links)并初始化(initializes) 类和接口。

Loading 的过程为找到特定名称的 class 或 interface 的二进制表示(可以从硬盘也可以从网络中加载)然后从该形式创建对应的 class 和 interface。

linking 是将 class 或 interface 组合成 JVM 的运行时状态，以被 JVM 执行。

Initialization 即执行 class 或 interface 的 `<clinit>` 方法。

## The Run-Time Constant Pool


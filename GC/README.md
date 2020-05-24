## GC and Storage Management

https://openjdk.java.net/groups/hotspot/docs/StorageManagement.html

分代收集算法

## HotSpot 实现

```
tree hotspot/src/share/vm/gc_implementation/ -L 1
hotspot/src/share/vm/gc_implementation/
├── concurrentMarkSweep
├── g1
├── parNew
├── parallelScavenge
└── shared
``` 

### 枚举根节点

根节点为 类静态变量、常量以及执行上下文等

使用 OopMap 记录所以引用

### 安全点

只在 SafePoint 记录 OopMap

主动式中断： 使 GC 发生时所有线程都跑到最近的安全点。通过设置一个标志，各个线程执行时轮询该标志，发现标志为真时主动中断挂起。

## 垃圾收集器

jdk1.7 默认垃圾收集器Parallel Scavenge（新生代）+Parallel Old（老年代）

jdk1.8 默认垃圾收集器Parallel Scavenge（新生代）+Parallel Old（老年代）

jdk1.9 默认垃圾收集器G1



-XX:+PrintCommandLineFlagsjvm参数可查看默认设置收集器类型

-XX:+PrintGCDetails亦可通过打印的GC日志的新生代、老年代名称判断


Java 7 - Parallel GC
Java 8 - Parallel GC
Java 9 - G1 GC
Java 10 - G1 GC

```
java -XX:+PrintCommandLineFlags -version
-XX:InitialHeapSize=166765248 -XX:MaxHeapSize=2668243968 -XX:+PrintCommandLineFlags -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC
openjdk version "1.8.0_212"
OpenJDK Runtime Environment (build 1.8.0_212-8u212-b01-1-b01)
OpenJDK 64-Bit Server VM (build 25.212-b01, mixed mode)
```

```
java -XX:+PrintGCDetails -version
openjdk version "1.8.0_212"
OpenJDK Runtime Environment (build 1.8.0_212-8u212-b01-1-b01)
OpenJDK 64-Bit Server VM (build 25.212-b01, mixed mode)
Heap
 PSYoungGen      total 47616K, used 1638K [0x000000078af80000, 0x000000078e480000, 0x00000007c0000000)
  eden space 40960K, 4% used [0x000000078af80000,0x000000078b119b18,0x000000078d780000)
  from space 6656K, 0% used [0x000000078de00000,0x000000078de00000,0x000000078e480000)
  to   space 6656K, 0% used [0x000000078d780000,0x000000078d780000,0x000000078de00000)
 ParOldGen       total 109568K, used 0K [0x0000000720e00000, 0x0000000727900000, 0x000000078af80000)
  object space 109568K, 0% used [0x0000000720e00000,0x0000000720e00000,0x0000000727900000)
 Metaspace       used 2156K, capacity 4480K, committed 4480K, reserved 1056768K
  class space    used 229K, capacity 384K, committed 384K, reserved 1048576K
```
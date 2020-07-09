`/bin/` 下的工具基本都是 `jdk/lib/tools.jar` 包装

代码位于 `openjdk-jdk8u-master\jdk\src\share\classes\sun\tools`

从报错堆栈看实现逻辑

```
.\jinfo -flags 6197
Attaching to process ID 6197, please wait...
Error attaching to process: Windbg Error: WaitForEvent failed!
sun.jvm.hotspot.debugger.DebuggerException: Windbg Error: WaitForEvent failed!
        at sun.jvm.hotspot.debugger.windbg.WindbgDebuggerLocal.attach0(Native Method)
        at sun.jvm.hotspot.debugger.windbg.WindbgDebuggerLocal.attach(WindbgDebuggerLocal.java:152)
        at sun.jvm.hotspot.HotSpotAgent.attachDebugger(HotSpotAgent.java:671)
        at sun.jvm.hotspot.HotSpotAgent.setupDebuggerWin32(HotSpotAgent.java:569)
        at sun.jvm.hotspot.HotSpotAgent.setupDebugger(HotSpotAgent.java:335)
        at sun.jvm.hotspot.HotSpotAgent.go(HotSpotAgent.java:304)
        at sun.jvm.hotspot.HotSpotAgent.attach(HotSpotAgent.java:140)
        at sun.jvm.hotspot.tools.Tool.start(Tool.java:185)
        at sun.jvm.hotspot.tools.Tool.execute(Tool.java:118)
        at sun.jvm.hotspot.tools.JInfo.main(JInfo.java:138)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at sun.tools.jinfo.JInfo.runTool(JInfo.java:108)
        at sun.tools.jinfo.JInfo.main(JInfo.java:76)
```

`openjdk-jdk8u-master/hotspot/agent/src/s
hare/classes/sun/jvm/hotspot/tools`

## Basic Tools

### JPS

Application to provide a listing of monitorable java processes.

获取指定 host 上活动的 jvm pid， Main Class

```
HostIdentifier hostId = arguments.hostId();
            MonitoredHost monitoredHost =
                    MonitoredHost.getMonitoredHost(hostId);

            // get the set active JVMs on the specified host.
            Set<Integer> jvms = monitoredHost.activeVms()
```

```
.\jps.exe -lv
12800 com.intellij.rt.execution.CommandLineWrapper -Dlog4j.debug -javaagent:C:\Program Files (x86)\JetBrains\IntelliJ IDEA Community Edition 2019.1.3\lib\idea_rt.jar=1587:C:\Program Files (x86)\JetBrains\IntelliJ IDEA Community Edition 2019.1.3\bin -Dfile.encoding=UTF-8
15268 sun.tools.jps.Jps -Dapplication.home=C:\Program Files\Java\jdk1.8.0_191 -Xms8m
```

### Jstat

<https://docs.oracle.com/javase/7/docs/technotes/tools/share/jstat.html>

Application to output jvmstat statistics exported by a target Java
Virtual Machine. The jstat tool gets its inspiration from the suite
of 'stat' tools, such as vmstat, iostat, mpstat, etc., available in
various UNIX platforms.

支持的参数如下

```
.\jstat -options
-class	Statistics on the behavior of the class loader.
-compiler	Statistics of the behavior of the HotSpot Just-in-Time compiler.
-gc	Statistics of the behavior of the garbage collected heap.
-gccapacity	Statistics of the capacities of the generations and their corresponding spaces.
-gccause	Summary of garbage collection statistics (same as -gcutil), with the cause of the last and current (if applicable) garbage collection events.
-gcnew	Statistics of the behavior of the new generation.
-gcnewcapacity	Statistics of the sizes of the new generations and its corresponding spaces.
-gcold	Statistics of the behavior of the old and permanent generations.
-gcoldcapacity	Statistics of the sizes of the old generation.
-gcpermcapacity	Statistics of the sizes of the permanent generation.
-gcutil	Summary of garbage collection statistics.
-printcompilation	HotSpot compilation method statistics.
```

```
 .\jstat -class 6196
Loaded  Bytes  Unloaded  Bytes     Time
  6632 13191.8        0     0.0       7.35
```


```
.\jstat -gcutil 6196
  S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT     GCT
  0.00  95.28  67.47  75.08  93.77  83.61     53    0.461     5    0.608    1.069
```

S0	Survivor space 0 utilization as a percentage of the space's current capacity.
S1	Survivor space 1 utilization as a percentage of the space's current capacity.
E	新生代 Eden space utilization as a percentage of the space's current capacity.
O	老年代 Old space utilization as a percentage of the space's current capacity.
P	Permanent space utilization as a percentage of the space's current capacity.
YGC	Number of young generation GC events.
YGCT	Young generation garbage collection time.
FGC	Number of full GC events.
FGCT	Full garbage collection time.
GCT	Total garbage collection time.

### jinfo

jinfo（Configuration Info for Java）的作用是实时查看和调整虚拟机各项参数。jinfo的-flag选项可以查看虚拟机启动时未被显式指定的参数的系统默认值。 jinfo还可以使用-sysprops选项把虚拟机进程的System.getProperties()的内容打印出来。

```
.\jinfo -flags 6196
Attaching to process ID 6196, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 25.191-b12
Non-default VM flags: -XX:AdaptiveSizePolicyWeight=90 -XX:-BytecodeVerificationLocal -XX:-BytecodeVerificationRemote -XX:CICompilerCount=4 -XX:GCTimeRatio=4 -XX:InitialHeapSize=104857600 -XX:MaxHeapSize=1073741824 -XX:MaxNewSize=357564416 -XX:MinHeapDeltaBytes=524288 -XX:NewSize=34603008 -XX:OldSize=70254592 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC
Command line:  -Declipse.application=org.eclipse.jdt.ls.core.id1 -Dosgi.bundles.defaultStartLevel=4 -Declipse.product=org.eclipse.jdt.ls.core.product -Dfile.encoding=utf8 -DwatchParentProcess=false -XX:+UseParallelGC -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -Dsun.zip.disableMemoryMapping=true -Xmx1G -Xms100m -Xverify:none
```

### jstak

Stack Trace for Java - Prints a stack trace of threads for a given process or core file or remote debug server.

OPTIONS
```
-F
Force a stack dump when 'jstack [-l] pid' does not respond.
-l
Long listing. Prints additional information about locks such as list of owned java.util.concurrent ownable synchronizers.
-m
prints mixed mode (both Java and native C/C++ frames) stack trace.
-h
prints a help message.

-help
prints a help message
```

也可以在代码中调用 Java.lang.Thread 类 getAllStackTraces() 方法获取虚拟机中所有的线程。

主要用于定位线程长时间停顿的原因(如：死锁、死循环、请求外部资源长时间未返回等)

## jwti

The JVM tool interface (JVM TI) is a native programming interface for use by tools. It provides both a way to inspect the state and to control the execution of applications running in the Java virtual machine (JVM). JVM TI supports the full breadth of tools that need access to JVM state, including but not limited to: profiling, debugging, monitoring, thread analysis, and coverage analysis tools.

https://docs.oracle.com/javase/7/docs/platform/jvmti/jvmti.html
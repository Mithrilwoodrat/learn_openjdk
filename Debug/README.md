# Build jdk

`git clone https://github.com/AdoptOpenJDK/openjdk-jdk8u.git`

## Install Requirements

Ubuntu:

```
sudo apt-get install libx11-dev libxext-dev libxrender-dev libxtst-dev libxt-dev 

sudo apt-get install -y libnx-x11-dev 

sudo apt-get install -y libcups2-dev libfreetype6-dev libasound2-dev libfontconfig1-dev 
```

## config 

Debug Level <https://stackoverflow.com/questions/53279665/what-is-openjdk-fastdebug-build-mode-how-is-it-different-from-release>
```
# Set the debug level
#    release: no debug information, all optimizations, no asserts.
#    optimized: no debug information, all optimizations, no asserts, HotSpot target is 'optimized'.
#    fastdebug: debug information (-g), all optimizations, all asserts
#    slowdebug: debug information (-g), no optimizations, all asserts
AC_DEFUN_ONCE([JDKOPT_SETUP_DEBUG_LEVEL],
```

需要设置为 slowdebug 避免g++优化后调试起来比较奇怪

```
./configure --with-debug-level=slowdebug
make JOBS=4 
```

更多的 config 参数可以参考 openjdk [文档](https://hg.openjdk.java.net/jdk-updates/jdk9u/raw-file/tip/common/doc/building.html#jdk-8-on-windows)

## debug

安装 eclipse cdt 以及 msys2 

<https://mirrors.tuna.tsinghua.edu.cn/help/msys2/>

使用 msys2 执行 `pacman -S mingw-w64-x86_64-gdb`

或者使用 VS Code 调试

## Main

以一个简单的 TestMain.java 为例

```
public class TestMain {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```

编译后将 TestMain.class 作为 java 的参数来调试 openjdk。

可执行程序 `java` 的入口为

`jdk/src/share/bin/main.c` 的 main 函数，

```
int
main(int argc, char **argv)
{
    int margc;
    char** margv;
    const jboolean const_javaw = JNI_FALSE;
    margc = argc;
    margv = argv;
    return JLI_Launch(margc, margv,
                   sizeof(const_jargs) / sizeof(char *), const_jargs,
                   sizeof(const_appclasspath) / sizeof(char *), const_appclasspath,
                   FULL_VERSION,
                   DOT_VERSION,
                   (const_progname != NULL) ? const_progname : *margv,
                   (const_launcher != NULL) ? const_launcher : *margv,
                   (const_jargs != NULL) ? JNI_TRUE : JNI_FALSE,
                   const_cpwildcard, const_javaw, const_ergo_class);
}
```

传入参数和全局静态变量，直接调用 `JLI_Launch` 函数

jdk/src/share/bin/java.c:JLI_Launch

```
int
JLI_Launch(int argc, char ** argv,              /* main argc, argc */
    ....
)
{
    ....
    //确认 JRE 版本 并设置 main_class，直接传入 java TestMain 这里不会设置 main_class 参数
    SelectVersion(argc, argv, &main_class);

    // 初始化一些环境
    CreateExecutionEnvironment(&argc, &argv,
                               jrepath, sizeof(jrepath),
                               jvmpath, sizeof(jvmpath),
                               jvmcfg,  sizeof(jvmcfg));
    // jvm_path 会被设置为  "xxx/jdk/lib/amd64/server/libjvm.so"
    if (!IsJavaArgs()) {
        SetJvmEnvironment(argc,argv);
    }

    ifn.CreateJavaVM = 0;
    ifn.GetDefaultJavaVMInitArgs = 0;

    // 加载 libjvm.so,并初始化 ifn 全局变量，获取默认参数等函数指针存放在 ifn 结构体中。在后续新建 JVM 线程前会调用
    if (!LoadJavaVM(jvmpath, &ifn)) {
        return(6);
    }

    ++argv;
    --argc;

    // 没有传入 classpath 这里默认会设置成 `.`
    if (IsJavaArgs()) {
        /* Preprocess wrapper arguments */
        TranslateApplicationArgs(jargc, jargv, &argc, &argv);
        if (!AddApplicationOptions(appclassc, appclassv)) {
            return(1);
        }
    } else {
        /* Set default CLASSPATH */
        cpath = getenv("CLASSPATH");
        if (cpath == NULL) {
            cpath = ".";
        }
        SetClassPath(cpath);
    }

    // 解析参数，不正确则结束程序
    if (!ParseArguments(&argc, &argv, &mode, &what, &ret, jrepath))
    {
        return(ret);
    }
    // ParseArguments 结束后会设置 what 变量为 TestMain

    /* Override class path if -jar flag was specified */
    if (mode == LM_JAR) {
        SetClassPath(what);     /* Override class path */
    }

    /* set the -Dsun.java.command pseudo property */
    SetJavaCommandLineProp(what, argc, argv);

    /* Set the -Dsun.java.launcher pseudo property */
    SetJavaLauncherProp();

    /* set the -Dsun.java.launcher.* platform properties */
    SetJavaLauncherPlatformProps();

    return JVMInit(&ifn, threadStackSize, argc, argv, mode, what, ret);
```

从参数和环境变量初始化好各类变量后，会调用 `JVMInit` 进入 JVM 真正的初始化流程。

jdk/src/solaris/bin/java_md_solinux.c 的 JVMInit，然后会调用到`ContinueInNewThread` 中，传入 JavaMain 函数指针到 `ContinueInNewThread0` 拉起新的线程启动 JVM

StackSize 在这里会被设置，若没有传入的话默认为 1024 kb，知乎上也有相关RednaxelaFX大佬的[回答](https://www.zhihu.com/question/27844575)。JVM为了控制 Java 线程的栈大小特意不使用进程的初始线程，这样操作系统 ulimit -s 设置的栈大小就不能影响 JVM。

具体的流程为 LoadJVM 中初始化 libjvm.so 和获取初始化参数的函数指针(`JNI_GetDefaultJavaVMInitArgs`)，然后在 `ContinueInNewThread` 中会调用该函数，

```
int
ContinueInNewThread(InvocationFunctions* ifn, jlong threadStackSize,
                    int argc, char **argv,
                    int mode, char *what, int ret)
{
    /*
     * If user doesn't specify stack size, check if VM has a preference.
     * Note that HotSpot no longer supports JNI_VERSION_1_1 but it will
     * return its default stack size through the init args structure.
     */
    if (threadStackSize == 0) {
      struct JDK1_1InitArgs args1_1;
      memset((void*)&args1_1, 0, sizeof(args1_1));
      args1_1.version = JNI_VERSION_1_1;
      ifn->GetDefaultJavaVMInitArgs(&args1_1);  /* ignore return value */
      if (args1_1.javaStackSize > 0) {
         threadStackSize = args1_1.javaStackSize;
      }
    }

    // 拉起一个新线程启动 JVM 并调用 main 方法
    { /* Create a new thread to create JVM and invoke main method */
      JavaMainArgs args;
      int rslt;

      args.argc = argc;
      args.argv = argv;
      args.mode = mode;
      args.what = what;
      args.ifn = *ifn;

      rslt = ContinueInNewThread0(JavaMain, threadStackSize, (void*)&args);
      /* If the caller has deemed there is an error we
       * simply return that, otherwise we return the value of
       * the callee
       */
      return (ret != 0) ? ret : rslt;
    }
}
```

传入的 JavaMain 为 `{int (void *)} 0x7fffff5975b7 <JavaMain>` 的一个全局函数指针。`ContinueInNewThread0` 在 Linux 下使用 pthread 拉起新线程

```
int
ContinueInNewThread0(int (JNICALL *continuation)(void *), jlong stack_size, void * args) {
    int rslt;
#ifndef __solaris__
    pthread_t tid;
    pthread_attr_t attr;
    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

    // 设置线程的 stacksize
    if (stack_size > 0) {
      pthread_attr_setstacksize(&attr, stack_size);
    }


    // 内存不足时还是会直接调用 JavaMain 而非建新线程
    if (pthread_create(&tid, &attr, (void *(*)(void*))continuation, (void*)args) == 0) {
      void * tmp;
      pthread_join(tid, &tmp);
      rslt = (int)tmp;
    } else {
     /*
      * Continue execution in current thread if for some reason (e.g. out of
      * memory/LWP)  a new thread can't be created. This will likely fail
      * later in continuation as JNI_CreateJavaVM needs to create quite a
      * few new threads, anyway, just give it a try..
      */
      rslt = continuation(args);
    }

    pthread_attr_destroy(&attr);
    return rslt;
}
```


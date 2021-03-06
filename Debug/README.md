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

更新，使用 slowdebug 时 hotspot 的 debug 信息会压缩到一个 libjvm.diz 文件里，使用起来不方便，可以使用 `disable-zip-debug-info` 禁用:

```
./configure --with-debug-level=slowdebug --with-target-bits=64 --disable-zip-debug-info
```

## debug

安装 eclipse cdt 以及 msys2 

<https://mirrors.tuna.tsinghua.edu.cn/help/msys2/>

使用 msys2 执行 `pacman -S mingw-w64-x86_64-gdb`

或者使用 VS Code 调试

gdb 命令可以参考 [100个gdb小技巧](https://www.gitbook.com/book/wizardforcel/100-gdb-tips)

VS Code debug console 中使用 -exec 可以执行 gdb 命令 

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

具体的流程为 LoadJVM 中初始化 libjvm.so 和获取初始化参数的函数指针(`JNI_GetDefaultJavaVMInitArgs`)，然后在 `ContinueInNewThread` 中会调用该函数，初始化 `args1_1` 为默认参数。

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

`jdk/src/solaris/bin/java_md_solinux.c:ContinueInNewThread0`

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

在 `jdk/src/share/bin/java.c:JavaMain` 中添加断点，继续运行后会断到该函数，此时执行 `-i threads` 得到线程情况如下:

```
-exec i threads
  Id   Target Id                                    Frame 
  1    Thread 0x7fffff390e00 (LWP 429) "java-debug" 0x00007fffff59e445 in ContinueInNewThread0 (continuation=0x7fffff5975b7 <JavaMain>, stack_size=1048576, args=0x7ffffffea9b0) at jdk/src/solaris/bin/java_md_solinux.c:1042
* 2    Thread 0x7ffffdc30700 (LWP 501) "java-debug" JavaMain (_args=0x7ffffffea9b0) at jdk/src/share/bin/java.c:356
```

### JavaMain

可以看到这时候线程2已经运行到了 JavaMain 中。首先调用 InitializeJVM 初始化 JVM，该函数会调用到 `hotspot/src/share/vm/prims/jni.cpp:JNI_CreateJavaVM`，使用 Atomic 保证只有一个线程能调用该函数(mutex无法跨线程上锁)，JVM 真正的初始化是从该函数开始的。

```
int JNICALL
JavaMain(void * _args)
{
    JavaMainArgs *args = (JavaMainArgs *)_args;
    int argc = args->argc;
    char **argv = args->argv;
    int mode = args->mode;
    char *what = args->what;
    InvocationFunctions ifn = args->ifn;

    JavaVM *vm = 0;
    JNIEnv *env = 0;
    jclass mainClass = NULL;
    jclass appClass = NULL; // actual application class being launched
    jmethodID mainID;
    jobjectArray mainArgs;
    int ret = 0;
    jlong start, end;

    RegisterThread();

    // 初始化 JVM 运行环境
    /* Initialize the virtual machine */
    start = CounterGet();
    if (!InitializeJVM(&vm, &env, &ifn)) {
        JLI_ReportErrorMessage(JVM_ERROR1);
        exit(1);
    }

    // 省略了一些打印输出的代码
    ..... 
    ret = 1;

    /*
     * Get the application's main class. 获取 main class
     */
    mainClass = LoadMainClass(env, mode, what);
    CHECK_EXCEPTION_NULL_LEAVE(mainClass);
    /*
     * In some cases when launching an application that needs a helper, e.g., a
     * JavaFX application with no main method, the mainClass will not be the
     * applications own main class but rather a helper class. To keep things
     * consistent in the UI we need to track and report the application main class.
     */
    appClass = GetApplicationClass(env);
    NULL_CHECK_RETURN_VALUE(appClass, -1);
    /*
     * PostJVMInit uses the class name as the application name for GUI purposes,
     * for example, on OSX this sets the application name in the menu bar for
     * both SWT and JavaFX. So we'll pass the actual application class here
     * instead of mainClass as that may be a launcher or helper class instead
     * of the application class.
     */
    PostJVMInit(env, appClass, vm);
    CHECK_EXCEPTION_LEAVE(1);
    /*
     * The LoadMainClass not only loads the main class, it will also ensure
     * that the main method's signature is correct, therefore further checking
     * is not required. The main method is invoked here so that extraneous java
     * stacks are not in the application stack trace.
     */
    mainID = (*env)->GetStaticMethodID(env, mainClass, "main",
                                       "([Ljava/lang/String;)V");
    CHECK_EXCEPTION_NULL_LEAVE(mainID);

    /* Build platform specific argument array */
    mainArgs = CreateApplicationArgs(env, argv, argc);
    CHECK_EXCEPTION_NULL_LEAVE(mainArgs);

    /* Invoke main method. */
    (*env)->CallStaticVoidMethod(env, mainClass, mainID, mainArgs);

    /*
     * The launcher's exit code (in the absence of calls to
     * System.exit) will be non-zero if main threw an exception.
     */
    ret = (*env)->ExceptionOccurred(env) == NULL ? 0 : 1;
    LEAVE();
}
```

#### InitializeJVM


`r = ifn->CreateJavaVM(pvm, (void **)penv, &args);`

调用 JNI 中的 CreateJavaVM 会继续调用 `hotspot/src/share/vm/runtime/thread.cpp` 中的 `create_vm`。

create_vm 主要步骤为初始化 os 模块，ostream模块，绑定主线程，加载 javaagent，初始化 JVM 同步机制，创建VM 主线程，加载异常类和 compile 类等等。

```
_JNI_IMPORT_OR_EXPORT_ jint JNICALL JNI_CreateJavaVM(JavaVM **vm, void **penv, void *args) {
    jint result = JNI_ERR;
    DT_RETURN_MARK(CreateJavaVM, jint, (const jint&)result);

    //使用 Atomic 保证只有一个线程能调用该函数(mutex无法跨线程上锁)
    if (Atomic::xchg(1, &vm_created) == 1) {
        return JNI_EEXIST;   // already created, or create attempt in progress
    }
    if (Atomic::xchg(0, &safe_to_recreate_vm) == 0) {
        return JNI_ERR;  // someone tried and failed and retry not allowed.
    }

    assert(vm_created == 1, "vm_created is true during the creation");

    ....
    // 调用 create_vm
    result = Threads::create_vm((JavaVMInitArgs*) args, &can_try_again);
  if (result == JNI_OK) {
    JavaThread *thread = JavaThread::current();
    /* thread is thread_in_vm here */
    *vm = (JavaVM *)(&main_vm);
    *(JNIEnv**)penv = thread->jni_environment();

    // Tracks the time application was running before GC
    RuntimeService::record_application_start();

    // Notify JVMTI
    if (JvmtiExport::should_post_thread_life()) {
       JvmtiExport::post_thread_start(thread);
    }

    EventThreadStart event;
    if (event.should_commit()) {
      event.set_javalangthread(java_lang_Thread::thread_id(thread->threadObj()));
      event.commit();
    }
```

在执行完 InitialzeJVM 后 gdb 中可以看到 20 个线程

```
1    Thread 0x7fffff390e00 (LWP 25823) "java-debug"      
  2    Thread 0x7ffffdc30700 (LWP 25838) "java-debug"      JavaMain (_args=0x7ffffffea9b0) at java.c:381
  .....
  11   Thread 0x7fffe21f0700 (LWP 25847) "VM Thread"       
  12   Thread 0x7fffe20e0700 (LWP 25848) "Reference Handl" 
  13   Thread 0x7fffe1fd0700 (LWP 25849) "Finalizer"       
  14   Thread 0x7fffe1a40700 (LWP 25850) "Signal Dispatch" 
  15   Thread 0x7fffe1930700 (LWP 25851) "C2 CompilerThre" 
  16   Thread 0x7fffe1820700 (LWP 25852) "C2 CompilerThre" 
  17   Thread 0x7fffe1710700 (LWP 25853) "C2 CompilerThre" 
  18   Thread 0x7fffe1600700 (LWP 25854) "C1 CompilerThre" 
  19   Thread 0x7fffe14f0700 (LWP 25855) "Service Thread"  
  20   Thread 0x7fffe13e0700 (LWP 25856) "VM Periodic Tas" 
```

其中 C2 线程数由 JVM 自身决定。

#### LoadMainClass

初始化好 JVM 虚拟机后便是加载 MainClass，通过 JNI GetStaticMethodID 函数调用对应的 java 代码。

```
/*
 * Loads a class and verifies that the main class is present and it is ok to
 * call it for more details refer to the java implementation.
 */
static jclass
LoadMainClass(JNIEnv *env, int mode, char *name)
{
    jmethodID mid;
    jstring str;
    jobject result;
    jlong start, end;
    jclass cls = GetLauncherHelperClass(env);
    NULL_CHECK0(cls);

    NULL_CHECK0(mid = (*env)->GetStaticMethodID(env, cls,
                "checkAndLoadMain",
                "(ZILjava/lang/String;)Ljava/lang/Class;"));

    str = NewPlatformString(env, name);
    CHECK_JNI_RETURN_0(
        result = (*env)->CallStaticObjectMethod(
            env, cls, mid, USE_STDERR, mode, str));
    
    .....

    return (jclass)result;
}
```

`jdk/src/share/classes/sun/launcher/LauncherHelper.java:checkAndLoadMain`

checkAndLoadMain Java代码会判断是 -jar 还是直接load class 文件，如果是 jar 则从 jar 包中找到 MainClass。然后调用 loadclass 方法加载。

```
    /**
     * This method does the following:
     * 1. gets the classname from a Jar's manifest, if necessary
     * 2. loads the class using the System ClassLoader
     * 3. ensures the availability and accessibility of the main method,
     *    using signatureDiagnostic method.
     *    a. does the class exist
     *    b. is there a main
     *    c. is the main public
     *    d. is the main static
     *    e. does the main take a String array for args
     * 4. if no main method and if the class extends FX Application, then call
    public static Class<?> checkAndLoadMain(boolean printToStderr,
                                            int mode,
                                            String what) {
        initOutput(printToStderr);
        // get the class name
        String cn = null;
        switch (mode) {
            case LM_CLASS:
                cn = what;
                break;
            case LM_JAR:
                cn = getMainClassFromJar(what);
                break;
            default:
                // should never happen
                throw new InternalError("" + mode + ": Unknown launch mode");
        }
        cn = cn.replace('/', '.');
        Class<?> mainClass = null;
        try {
            mainClass = scloader.loadClass(cn);
        } catch (NoClassDefFoundError | ClassNotFoundException cnfe) {
            if (System.getProperty("os.name", "").contains("OS X"){
                .....
            } else {
                abort(cnfe, "java.launcher.cls.error1", cn);
            }
        }
        // set to mainClass
        appClass = mainClass;

        /*
         * Check if FXHelper can launch it using the FX launcher. In an FX app,
         * /

        validateMainClass(mainClass);
        return mainClass;
    }

```

scloader 的初始化语句为 `private static final ClassLoader scloader = ClassLoader.getSystemClassLoader();`

对应 `jdk/src/share/classes/java/lang/ClassLoader` 类

```
public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }


// 可以看到 loadClass 函数默认是先调用 parent 的 loadClass，若
// parent 不存在即为 SystemClassLoader,则进入 findBootstrapClassOrNull。
// 还找不到的话会调用 findClass 继续查找

protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }

                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    long t1 = System.nanoTime();
                    c = findClass(name);

                    // this is the defining class loader; record the stats
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
```

默认的 SystemClassLoader 即上述 ClassLoader 基类最会调用 `findBootstrapClassOrNull` 最后会调用 `findBootstrapClass` JNI 方法。

```
private Class<?> findBootstrapClassOrNull(String name)
{
    if (!checkName(name)) return null;

    return findBootstrapClass(name);
}
// return null if not found
private native Class<?> findBootstrapClass(String name);
```

#### findBootstrapClass

findBootstrapClass JNI 调用对应函数 `jdk/src/share/native/java/lang/ClassLoader.c:Java_java_lang_ClassLoader_findBootstrapClass`

```
/*
 * Returns NULL if class not found.
 */
JNIEXPORT jclass JNICALL
Java_java_lang_ClassLoader_findBootstrapClass(JNIEnv *env, jobject loader,
                                              jstring classname)
{
    char *clname;
    jclass cls = 0;
    char buf[128];

    if (classname == NULL) {
        return 0;
    }

    clname = getUTF(env, classname, buf, sizeof(buf));
    if (clname == NULL) {
        JNU_ThrowOutOfMemoryError(env, NULL);
        return NULL;
    }
    VerifyFixClassname(clname);

    if (!VerifyClassname(clname, JNI_TRUE)) {  /* expects slashed name */
        goto done;
    }

    cls = JVM_FindClassFromBootLoader(env, clname);

 done:
    if (clname != buf) {
        free(clname);
    }

    return cls;
}
```

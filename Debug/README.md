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

```
./configure  --enable-debug 
make JOBS=4 
```

## debug

安装 eclipse cdt 以及 msys2 

<https://mirrors.tuna.tsinghua.edu.cn/help/msys2/>

使用 msys2 执行 `pacman -S mingw-w64-x86_64-gdb`

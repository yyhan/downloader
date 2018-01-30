@echo off
rem ---------------------------------------------------------------------------
rem usage: downloader [-h] [-o <outputDir>] [-t <threadNumber>] -u <urlsFile>
rem  -h,--help                          输出此帮助信息
rem  -o,--outputDir <outputDir>         文件保存路径
rem  -n,--threadNumber <threadNumber>   执行下载请求的线程数量,<1,100>
rem  -t,--type <type>   文件类型，1、文本文件；2、二进制文件
rem  -u,--urlsFile <urlsFile>           下载链接集合文件，每行作为一个下载链接
rem ---------------------------------------------------------------------------

java -jar lib\${project.artifactId}-${project.version}.jar -u E:\temp\urls.txt -o E:\temp\output\ -n 1 -t 2
pause


@echo off
rem ---------------------------------------------------------------------------
rem usage: downloader [-h] [-o <outputDir>] [-t <threadNumber>] -u <urlsFile>
rem  -h,--help                          输出此帮助信息
rem  -o,--outputDir <outputDir>         文件保存路径
rem  -t,--threadNumber <threadNumber>   执行下载请求的线程数量,<1,100>
rem  -u,--urlsFile <urlsFile>           下载链接集合文件，每行作为一个下载链接
rem ---------------------------------------------------------------------------

java -jar lib\${project.artifactId}-${project.version}.jar -u D:\test\urls.txt -o D:\test\output\ -t 10
pause


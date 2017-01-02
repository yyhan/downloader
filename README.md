## downloader
用于从一个文本文件里读取下载链接并批量下载的java应用程序。
## 使用说明
```
usage: downloader [-h] [-o <outputDir>] [-t <threadNumber>] -u <urlsFile>
 -h,--help                          输出此帮助信息
 -o,--outputDir <outputDir>         文件保存路径
 -t,--threadNumber <threadNumber>   执行下载请求的线程数量,<1,100>
 -u,--urlsFile <urlsFile>           下载链接集合文件，每行作为一个下载链接
```
例如：  
以下命令指定下载链接集合文件为`urls.txt`，下载的文件保存在`D:/test/output/`目录下，同时指定下载的线程数为`10`。
``` batch
java downloader.jar -u D:/test/urls.txt -o D:/test/output/ -t 10
```
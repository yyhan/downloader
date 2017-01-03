package com.cloudin.downloader;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YFHan on 2017/1/1 0001.
 */
public class App {

    private Options options;

    public App() {
        this.options = new Options();
        options.addOption(Option.builder("t").longOpt("threadNumber").hasArg(true).argName("threadNumber").type(java.lang.Integer.class).required(false).desc("执行下载请求的线程数量,<1,100>").build());
        options.addOption(Option.builder("u").longOpt("urlsFile").hasArg(true).argName("urlsFile").type(java.lang.String.class).required(true).desc("下载链接集合文件，每行作为一个下载链接").build());
        options.addOption(Option.builder("o").longOpt("outputDir").hasArg(true).argName("outputDir").type(java.lang.String.class).required(false).desc("文件保存路径").build());
        options.addOption(Option.builder("h").longOpt("help").argName("help").type(java.lang.String.class).required(false).desc("输出此帮助信息").build());
    }

    public static void main(String[] args) {
        App app = new App();

        CommandLine cmd = app.parseCmd(args);
        if (cmd == null || !cmd.hasOption("urlsFile") || cmd.hasOption("help")) {
            app.printHelp();
        } else {
            for (String line : cmd.getArgList()) {
                System.out.println(line);
            }
            String outputDir = cmd.getOptionValue("outputDir");


            String urlsFile = cmd.getOptionValue("urlsFile");

            Integer threadNumber = null;
            try {
                threadNumber = Integer.parseInt(cmd.getOptionValue("threadNumber", "1"));
            } catch (NumberFormatException e) {
                System.err.println("指定的线程数量参数格式错误");
                return;
            }

            System.out.printf("下载链接集合：%s\n", urlsFile);
            System.out.printf("保存路径：%s\n", outputDir);
            System.out.printf("执行线程数：%s\n", threadNumber);

            if (!app.checkOutPutDir(outputDir) || !app.checkUrlsFile(urlsFile)) {
                return;
            }
            if (!outputDir.endsWith("/")) {
                outputDir += "/";
            }

            List<String> lines = parseUrls(urlsFile);
            System.out.printf("预期总下载数：%d\n", lines.size());
            if (threadNumber == 1) {
                if (lines == null) {
                    System.err.println("指定的下载链接为空");
                    return;
                } else {
                    Downloader downloader = Downloader.newDownload();
                    for (String line : lines) {
                        downloader.download(line, outputDir);
                    }
                    System.out.println("==== 下载结束 ====");
                }
            } else {

                String[] urls = lines.toArray(new String[]{});

                int len = urls.length;
                int groupCount = len / threadNumber;
                int less = len % groupCount;
                ThreadOverEventListener threadOverEventListener= new ThreadOverEventListener(len, threadNumber);

                for (int i = 0, o = threadNumber - 1; i < o; i++) {
                    new DownloadThread(ArrayUtils.subarray(urls, i * groupCount, (i + 1) * groupCount), outputDir, threadOverEventListener).start();
                }
                new DownloadThread(ArrayUtils.subarray(urls, (threadNumber - 1) * groupCount, len), outputDir, threadOverEventListener).start();

            }
        }
    }

    public interface IThreadOverEventListener {
        void exec(DownloadInfo info);
    }

    public static class ThreadOverEventListener implements IThreadOverEventListener {

        private int threadNumber;
        private List<String> displayStr;
        private DownloadInfo downloadInfo;
        private long startTime;


        public ThreadOverEventListener(int expectedCount, int threadNumber) {
            this.displayStr = new ArrayList<>(threadNumber);
            this.threadNumber = threadNumber;
            this.downloadInfo = new DownloadInfo(expectedCount, 0, 0, 0);
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public synchronized void exec(DownloadInfo info) {
            System.out.printf("[%s] is over \n", Thread.currentThread().getName());
            displayStr.add(String.format("[%s] expectedCount = %d, successCount = %d, errorCount = %d, taking = %d ms",
                    Thread.currentThread().getName(), info.getExpectedCount(), info.getSuccessCount(), info.getErrorCount(), info.getTimeConsume()));
            downloadInfo.setSuccessCount(downloadInfo.getSuccessCount() + info.getSuccessCount());
            downloadInfo.setErrorCount(downloadInfo.getErrorCount() + info.getErrorCount());
            downloadInfo.setTimeConsume(System.currentTimeMillis() - startTime);
            if (displayStr.size() == this.threadNumber) {
                System.out.println("==== 下载结束 ====");
                System.out.println("各线程下载情况：");

                for (String s : displayStr) {
                    System.out.println(s);
                }
                System.out.println("下载统计：");
                System.out.printf("expectedCount = %d, successCount = %d, errorCount = %d, taking = %d ms\n\n",
                        downloadInfo.getExpectedCount(), downloadInfo.getSuccessCount(), downloadInfo.getErrorCount(), downloadInfo.getTimeConsume());
                System.out.printf("bye bye\n\n");
                System.exit(0);
            }
        }
    }

    public static class DownloadThread extends Thread {

        private String[] urls;
        private String outputDir;
        private ThreadOverEventListener listener;

        public DownloadThread(String[] urls, String outputDir, ThreadOverEventListener listener) {
            super();
            this.urls = urls;
            this.outputDir = outputDir;
            this.listener = listener;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            Downloader downloader = Downloader.newDownload();
            for (String line : urls) {
                downloader.download(line, outputDir);
            }
            long end = System.currentTimeMillis();
            listener.exec(new DownloadInfo(urls.length, downloader.getSuccessCount(), downloader.getErrCount(), end - start));
        }
    }

    public static class DownloadInfo {
        private int expectedCount;
        private int successCount;
        private int errorCount;
        private long timeConsume;

        public DownloadInfo(int expectedCount, int successCount, int errorCount, long timeConsume) {
            this.expectedCount = expectedCount;
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.timeConsume = timeConsume;
        }

        public int getExpectedCount() {
            return expectedCount;
        }

        public void setExpectedCount(int expectedCount) {
            this.expectedCount = expectedCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(int errorCount) {
            this.errorCount = errorCount;
        }

        public long getTimeConsume() {
            return timeConsume;
        }

        public void setTimeConsume(long timeConsume) {
            this.timeConsume = timeConsume;
        }
    }

    public boolean checkUrlsFile(String urlsFilePath) {
        File urlsFile = new File(urlsFilePath);
        if (urlsFile.exists() && urlsFile.isFile()) {
            return true;
        }
        System.err.println("指定下载链接集合文件路径是无效的");
        return false;
    }

    public boolean checkOutPutDir(String outputDirPath) {
        File outputDir = new File(outputDirPath);
        if (outputDir.exists()) {
            if (outputDir.isDirectory()) {
                return true;
            } else {
                System.err.println("指定的输出路径有同名文件存在");
                return false;
            }
        } else {
            if (outputDir.mkdir()) {
                return true;
            } else {
                System.err.println("指定的输出路径不存在，且创建该路径失败");
                return false;
            }
        }
    }

    public CommandLine parseCmd(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(this.options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("downloader", this.options, true);
    }

    public static List<String> parseUrls(String filePath) {
        List<String> lines = null;
        try {
            File urlsFile = new File(filePath);
            lines = IOUtils.readLines(new FileInputStream(urlsFile), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}

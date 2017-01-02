package com.cloudin.downloader;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        options.addOption(Option.builder("h").longOpt("help").argName("help").type(java.lang.String.class).required(false).desc("文件保存路径").build());
    }

    public static void main(String[] args) {
        App app = new App();

        CommandLine cmd = app.parseCmd(args);
        if (cmd == null || !cmd.hasOption("urlsFile") || cmd.hasOption("h") || cmd.hasOption("help")) {
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
                ThreadOverEventListener threadOverEventListener;
                if (less > 0) {
                    threadOverEventListener = new ThreadOverEventListener(threadNumber + 1);
                    new DownloadThread(ArrayUtils.subarray(urls, threadNumber * groupCount, len), outputDir, threadOverEventListener).start();
                } else {
                    threadOverEventListener = new ThreadOverEventListener(threadNumber);
                }

                for (int i = 0; i < threadNumber; i++) {
                    new DownloadThread(ArrayUtils.subarray(urls, i, (i + 1) * groupCount), outputDir, threadOverEventListener).start();
                }

            }
        }
    }

    public interface IThreadOverEventListener {
        void exec(Thread thread);
    }

    public static class ThreadOverEventListener implements IThreadOverEventListener {

        private AtomicInteger counter;


        public ThreadOverEventListener(int countValue) {
            counter = new AtomicInteger(countValue);
        }

        @Override
        public void exec(Thread thread) {
            System.out.printf("[%s] is over \n", thread.getName());
            int v = counter.decrementAndGet();
            if (v == 0) {
                System.out.println("==== 下载结束 ====");
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
            Downloader downloader = Downloader.newDownload();
            for (String line : urls) {
                downloader.download(line, outputDir);
            }
            System.out.printf("[%s] expectedCount = %05d, successCount = %05d, errorCount = %05d\n", Thread.currentThread().getName(), urls.length, downloader.getCount(), downloader.getErrCount());
            listener.exec(Thread.currentThread());
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

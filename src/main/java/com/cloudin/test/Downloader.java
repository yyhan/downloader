package com.cloudin.test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by YFHan on 2017/1/2 0002.
 */
public class Downloader {

    private OkHttpClient httpClient;

    private Pattern pattern = Pattern.compile(".*\\/(.*)\\?");

    private int count = 0;
    private int errCount = 0;


    public Downloader() {
        httpClient = new OkHttpClient();
    }

    public static Downloader newDownload() {
        return new Downloader();
    }

    public void download(String url, String outputDir) {

        String tagFileName = getFileName(url);
        if (tagFileName == null) {
            System.out.printf("[%s][%05d][文件名为空]\n", Thread.currentThread().getName(), count++);
            return;
        }
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            String resTxt = response.body().string();

            File tagFile = new File(outputDir + tagFileName);
            if (!tagFile.exists()) {
                tagFile.createNewFile();
            }

            IOUtils.write(resTxt, new FileOutputStream(tagFile), "UTF-8");
            System.out.printf("[%s][%05d][写入成功] %s\n", Thread.currentThread().getName(), count++, tagFileName);
        } catch (IOException e) {
            System.err.print(e);
        }
    }

    public int getCount() {
        return this.count;
    }

    public int getErrCount() {
        return this.errCount;
    }
    private String getFileName(String url) {
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }

    }
}

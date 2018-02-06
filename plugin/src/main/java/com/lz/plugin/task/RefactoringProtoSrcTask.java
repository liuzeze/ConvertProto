package com.lz.plugin.task;


import com.lz.plugin.utils.UnicodeReader;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import download.DownloadManager;

/**
 * Created by xiaoyulaoshi on 2018/1/26.
 */

public class RefactoringProtoSrcTask extends DefaultTask {
    private String reqUrl = "";
    private String businessName = "";
    private String convertPackageName = "cn.com.jsj.GCTravelTools.mvp.bean.protobuf";
    private String downloadSingleProtos;

    private String targetDirectoryPath;
    private String sourceDirectoryPath;
    private LinkedHashMap<String, String> mDownLoadUrlMap = new LinkedHashMap<String, String>();
    private String baseProtoName;
    private ArrayList<String> dowmFiles = new ArrayList<>();

    @TaskAction
    public void readProtoFile() throws Exception {

        baseProtoName = baseProtoName + ",BaseRequest_p,BaseResponse_p,AppSource,BaseRequest,DataCommission,LanguageVersion,SourceWay,BaseResponse,MemberLoginResult,MoHotelRequestBase,MoHotelResponseBase,RcBaseRequest,VcodeType,ZResponse,ZRequest,ClientLanguage";
        writeLog("start_delete_dir=============="+reqUrl+reqUrl.length());
        //Empty file
        writeLog("start clear sourceDirectoryPath file \n");
        dropDirectory(sourceDirectoryPath, businessName);
        writeLog("end file sourceDirectoryPath \n");
        writeLog("start clear targetDirectoryPath file \n");
        dropDirectory(targetDirectoryPath, businessName);
        writeLog("end file targetDirectoryPath \n");
        //Parseing HTML.
        writeLog("start  Parseing HTML  \n");
        analysisDownLoadUrl(reqUrl);
        writeLog("Parsing is complete \n");
        if (downloadSingleProtos != null) {
            if (downloadSingleProtos.trim().length() != 0) {
                Collections.addAll(dowmFiles, downloadSingleProtos.split(","));
            }
        }
        //Download the proto file.
        writeLog("Start the download \n");
        downloadProtoFile(businessName);
        writeLog("download completes \n");

        //Start splitting the proto file.
        writeLog("Start splitting the proto file.. \n");
        splitProtoFile(sourceDirectoryPath, businessName);
        writeLog("Split the proto file to complete. \n");

        //Start adding references.
        writeLog("Start adding references. \n");
        addImportToProtoFile(targetDirectoryPath, "");
        writeLog("Add reference completion. \n");
    }

    /**
     * Parseing HTML.
     *
     * @param apiUrl
     */
    private void analysisDownLoadUrl(String apiUrl) throws Exception {

        mDownLoadUrlMap.clear();

        Document document = Jsoup.connect(apiUrl).get();
        Elements elements = document.select("table[class=help-page-table] > tbody > tr");

        for (Element element : elements) {
            Elements select = element.select("td[class=api-documentation] a");
            for (Element element1 : select) {
                String url = element1.select("a")
                        .attr("href")
                        .replace("?help&amp;m2", "&down")
                        .replace("?help&m2", "&down")
                        .replace("&amp;dll", "&dll");
                String text = element1.select("a").text();
                mDownLoadUrlMap.put(text, apiUrl + url);

                writeLog("dowmload url:\n" + text + "  " + apiUrl + url + " \n");
            }
        }


    }


    /**
     * Empty file
     *
     * @param filePath
     * @param businessName
     */
    private void dropDirectory(String filePath, String businessName) throws Exception {

        File file = new File(filePath + "/" + businessName);

        file.mkdirs();
        File[] files = file.listFiles();
        if (null != files) {
            for (File tempFile : files) {
                if (tempFile.isFile()) {
                    boolean deleteState = tempFile.delete();
                }
                if (tempFile.isDirectory()) {
                    dropDirectory(filePath, filePath + "/" + businessName + "/" + tempFile.getName());

                    tempFile.delete();
                }
            }
        } else {
            writeLog(filePath + " file is empty \n");
        }
    }

    /**
     * Download the proto file.
     *
     * @param businessName
     */
    private void downloadProtoFile(String businessName) throws Exception {
        CountDownLatch latch = new CountDownLatch(dowmFiles.size() == 0 ? mDownLoadUrlMap.size() : dowmFiles.size());
        for (Map.Entry<String, String> entry : mDownLoadUrlMap.entrySet()) {
            boolean aplitAll = isAllProtoFiles(entry.getKey());
            if (aplitAll) {
                DownloadManager downloadManager = new DownloadManager(latch);
                downloadManager.download(entry.getValue(), sourceDirectoryPath, businessName, entry.getKey());
                downloadManager.setOnErrorListener(new DownloadManager.OnErrorListener() {
                    @Override
                    public void onError(String error) {
                        try {
                            writeLog(error);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        latch.await();

    }


    /**
     * Start splitting the proto file.
     *
     * @param businessName
     * @param sourceDirectoryPath
     */
    private void splitProtoFile(String sourceDirectoryPath, String businessName) throws Exception {

        File file = new File(sourceDirectoryPath + "/" + businessName);
        File[] files = file.listFiles();
        ArrayList<String> baseProtoLists = new ArrayList<>();
        Collections.addAll(baseProtoLists, baseProtoName.split(","));
        if (null != files) {
            for (File tempFile : files) {
                boolean aplitAll = isAllProtoFiles(tempFile.getName().replace(".proto", ""));
                if (tempFile.isFile() && tempFile.getName().endsWith(".proto") && aplitAll) {

                    FileInputStream input = new FileInputStream(tempFile);

                    UnicodeReader ur = new UnicodeReader(input, "UTF-8");

                    BufferedReader bufferedReader = new BufferedReader(ur);

                    String textLine = null;

                    File targetFile2 = new File(targetDirectoryPath + "/" + businessName, tempFile.getName());
                    FileOutputStream targetOutput2 = new FileOutputStream(targetFile2);
                    BufferedWriter targetBw2 = new BufferedWriter(new OutputStreamWriter(targetOutput2));
                    boolean isBaseProto2 = true;

                    while ((textLine = bufferedReader.readLine()) != null) {

                        if ((textLine.startsWith("message") && textLine.endsWith("{") ||
                                textLine.startsWith("enum") && textLine.endsWith("{"))) {
                            String temp1 =
                                    textLine.replace("message", "")
                                            .replace("enum", "")
                                            .replace("{", "");

                            String fileName = temp1.replace("{", "").trim();

                            if (baseProtoLists.indexOf(fileName) != -1) {
                                isBaseProto2 = false;
                            } else {
                                isBaseProto2 = true;
                            }

                        }

                        if (!textLine.contains("syntax") && !textLine.equals("")) {
                            if (null != targetBw2 && isBaseProto2) {
                                targetBw2.write(textLine + "\n");
                            }
                        }

                        if (textLine.endsWith("}")) {
                            if (!isBaseProto2) {
                                isBaseProto2 = true;
                            }
                        }
                    }

                    if (null != targetBw2) {
                        targetBw2.close();
                    }

                    bufferedReader.close();
                    ur.close();

                }

                if (tempFile.isDirectory()) {
                    splitProtoFile(sourceDirectoryPath + "/" + businessName + "/" + tempFile.getName(), "");
                }
            }
        } else {
            writeLog("The source file directory does not exist.(Split the proto file ) \n");
        }

    }

    private boolean isAllProtoFiles(String fileName) throws Exception {
        boolean aplitAll = true;
        if (dowmFiles.size() == 0) {
            aplitAll = true;
        } else {
            if (dowmFiles.indexOf(fileName) != -1) {
                aplitAll = true;
            } else {
                aplitAll = false;
            }
        }
        return aplitAll;
    }


    /**
     * Start adding references.
     *
     * @param subfileName
     * @param targetDirectoryPath
     */

    private void addImportToProtoFile(String targetDirectoryPath, String subfileName) throws Exception {

        File file = new File(targetDirectoryPath + "/" + businessName);
        File[] filesList = file.listFiles();
        ArrayList<String> baseProtoLists = new ArrayList<>();
        Collections.addAll(baseProtoLists, baseProtoName.split(","));
        if (null != filesList) {
            for (File tempFile : filesList) {
                boolean aplitAll = isAllProtoFiles(tempFile.getName().replace(".proto", ""));
                if (tempFile.isFile() && tempFile.getName().endsWith(".proto") && aplitAll) {

                    FileInputStream input = new FileInputStream(tempFile);

                    UnicodeReader ur = new UnicodeReader(input, "UTF-8");

                    BufferedReader bufferedReader = new BufferedReader(ur);
                    bufferedReader.mark((int) tempFile.length() + 1);

                    String textLine = null;

                    ArrayList<String> importList = new ArrayList<>();

                    boolean needImport = false;

                    ArrayList<String> importInfor = new ArrayList<>();

                    while ((textLine = bufferedReader.readLine()) != null) {

                        if ((textLine.startsWith("message") && textLine.endsWith("{"))) {
                            needImport = true;
                        }
                        if (textLine.contains("syntax = \"")) {
                            importInfor.add("syntax");
                        }
                        if (textLine.contains("option java_package = \"" + convertPackageName + "." + businessName + subfileName + "\";")) {
                            importInfor.add("option java_package");
                        }
                        if (textLine.contains("option java_multiple_files =")) {
                            importInfor.add("option java_multiple_files");
                        }

                        if (textLine.contains("import \"")) {
                            importInfor.add(textLine);
                        }

                        if (needImport) {
                            if (!(textLine.startsWith("message") && textLine.endsWith("{")) && !textLine.endsWith("}")) {

                                String[] wordStr = textLine.trim().split(" ");

                                if (wordStr.length > 0) {
                                    String typeStr = wordStr[1];


                                    switch (typeStr) {
                                        case "double":
                                            break;
                                        case "repeated":
                                            break;
                                        case "float":
                                            break;
                                        case "int32":
                                            break;
                                        case "int64":
                                            break;
                                        case "uint32":
                                            break;
                                        case "uint64":
                                            break;
                                        case "sint32":
                                            break;
                                        case "sint64":
                                            break;
                                        case "fixed32":
                                            break;
                                        case "fixed64":
                                            break;
                                        case "sfixed32":
                                            break;
                                        case "bool":
                                            break;
                                        case "string":
                                            break;
                                        case "bytes":
                                            break;
                                        default:
                                            if (!importList.contains(typeStr)) {
                                                importList.add(typeStr);
                                            }

                                            break;
                                    }
                                }
                            }
                        }

                    }


                    File targetFile;
                    FileOutputStream targetOutput;
                    BufferedWriter targetBw = null;

                    targetFile = new File(targetDirectoryPath + "/" + businessName, tempFile.getName());
                    targetOutput = new FileOutputStream(targetFile);
                    targetBw = new BufferedWriter(new OutputStreamWriter(targetOutput));
                    if (!importInfor.contains("syntax")) {
                        targetBw.write("syntax = \"proto2\";\n");
                    }
                    if (!importInfor.contains("option java_package")) {
                        targetBw.write("option java_package = \"" + convertPackageName + "." + businessName + subfileName + "\";\n");
                    }
                    if (!importInfor.contains("option java_multiple_files")) {
                        targetBw.write("option java_multiple_files = true;\n\n");
                    }

                    bufferedReader.reset();

                    if (needImport) {
                        for (int i = 0; i < importList.size(); i++) {
                            String importName = importList.get(i);
                            if (baseProtoLists.indexOf(importName) != -1) {//Import only base classes
                                if (!importInfor.contains("import \"" + importName + ".proto\";")) {
                                    targetBw.write("import \"" + importName + ".proto\";\n");
                                    if (i == importList.size() - 1) {
                                        targetBw.write("\n");
                                    }
                                }
                            }
                        }

                        while ((textLine = bufferedReader.readLine()) != null) {
                            targetBw.write(textLine + "\n");
                        }

                        targetBw.close();
                    } else {

                        while ((textLine = bufferedReader.readLine()) != null) {
                            targetBw.write(textLine + "\n");
                        }

                        targetBw.close();
                    }

                    bufferedReader.close();
                    ur.close();
                }

                if (tempFile.isDirectory()) {
                    addImportToProtoFile(targetDirectoryPath + "/" + tempFile.getName(), "." + tempFile.getName());
                }
            }
        } else {
            writeLog("The source file directory does not exist.(adding refrence) \n");
        }

    }

    /**
     * @param sourceDirectoryPath
     */
    public void setSourceDirectoryPath(String sourceDirectoryPath) {
        this.sourceDirectoryPath = sourceDirectoryPath;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setConvertPackageName(String convertPackageName) {
        this.convertPackageName = convertPackageName;
    }

    /**
     * @param targetDirectoryPath
     */
    public void setTargetDirectoryPath(String targetDirectoryPath) {
        this.targetDirectoryPath = targetDirectoryPath;
    }

    public void setBaseProtoName(String baseProtoName) {
        this.baseProtoName = baseProtoName;
    }

    public void setDownloadSingleProtos(String downloadSingleProtos) {
        this.downloadSingleProtos = downloadSingleProtos;
    }


    public void writeLog(String content) throws Exception {
        System.out.println(content);
    }
}

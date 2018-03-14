package com.lz.plugin.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by liuze on 2018/1/26.
 */

public class ConverProtoTask extends DefaultTask {
    private String convertProtoUrl = "app/src/main/java";
    private String convertSingleProtos;
    private String convertBusinessName;


    private String targetDirectoryPath;
    private File mConvertFilePath;

    @TaskAction
    public void convertProtoFile() throws Exception {
        writeLog("To transform \n");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@echo off\n" + targetDirectoryPath.charAt(0) + ":\ncd " + this.targetDirectoryPath + "\n");
        convertProtoUrl = new File(mConvertFilePath, convertProtoUrl).getAbsolutePath();
        writeLog("Convert the Java file directory.===== " + convertProtoUrl + "\n ");
        convertJavaFiles(targetDirectoryPath + "/" + convertBusinessName, convertBusinessName + "/", stringBuilder);
        writeLog("Conversion success. \n ");

        writeLog("Start running the transformation command. \n ");
        runningCmd(stringBuilder);
        writeLog("Run the transformation command successfully..\n ");
    }

    /**
     * Run the transformation command
     *
     * @param stringBuilder
     */
    private void runningCmd(StringBuilder stringBuilder) {
        Process process = null;
        try {
            File sourceFile = new File(targetDirectoryPath);
            File apiJsonFile = new File(sourceFile.getParent(), "Proto.bat");
            FileOutputStream apiJsonOutput = new FileOutputStream(apiJsonFile);
            stringBuilder.append("echo Job is done");
            apiJsonOutput.write(stringBuilder.toString().getBytes("UTF-8"));
            apiJsonOutput.close();
            String path = sourceFile.getParent() + "/Proto.bat";
            Runtime run = Runtime.getRuntime();

            process = run.exec("cmd.exe /c " + path);
            //Letting go of this code will be particularly slow.

            writeLog("===========================Command line  Error  Infor==============================\n");

            BufferedReader error = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String line;
            while ((line = error.readLine()) != null) {
                writeLog(line);
            }
            error.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                writeLog(e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } finally {
            if (process != null) {
                try {
                    process.getErrorStream().close();
                    process.getInputStream().close();
                    process.getOutputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void convertJavaFiles(String targetDirectoryPath, String subFileName, StringBuilder stringBuilder) throws Exception {
        ArrayList<String> protoNames = new ArrayList<>();
        if (convertSingleProtos != null) {
            if (convertSingleProtos.trim().length() != 0) {
                if (convertSingleProtos != null) {
                    Collections.addAll(protoNames, convertSingleProtos.split(","));
                }
                writeLog("==============\n " + protoNames.toString());
            }
        }

        File file = new File(targetDirectoryPath);
        File[] filesList = file.listFiles();
        if (null != filesList) {
            for (File tempFile : filesList) {
                if (tempFile.isFile() && tempFile.getName().endsWith(".proto")) {
                    if (protoNames.size() == 0) {
                        stringBuilder.append("protoc --javanano_out=enum_style=java,optional_field_style=default,parcelable_messages=true:" + convertProtoUrl + " " + subFileName + tempFile.getName() + "\n");
                    } else {
                        if (protoNames.indexOf(tempFile.getName().replace(".proto", "")) != -1) {
                            stringBuilder.append("protoc --javanano_out=enum_style=java,optional_field_style=default,parcelable_messages=true:" + convertProtoUrl + " " + subFileName + tempFile.getName() + "\n");
                        }
                    }
                    writeLog("protoc --javanano_out=enum_style=java,optional_field_style=default,parcelable_messages=true:" + convertProtoUrl + " " + subFileName + tempFile.getName() + "\n");
                }
                if (tempFile.isDirectory()) {
                    convertJavaFiles(targetDirectoryPath + "/" + tempFile.getName(), tempFile.getName() + "/", stringBuilder);

                }
            }
        } else {
            writeLog("The source file directory does not exist.(adding refrence) \n");
        }


    }

    /**
     * @param targetDirectoryPath
     */
    public void setTargetDirectoryPath(String targetDirectoryPath) {
        this.targetDirectoryPath = targetDirectoryPath;
    }


    public void setConvertProtoUrl(String convertProtoUrl) {
        this.convertProtoUrl = convertProtoUrl;
    }

    public void setConvertSingleProtos(String convertSingleProtos) {
        this.convertSingleProtos = convertSingleProtos;
    }

    public void setConvertFilePath(File convertFilePath) {
        mConvertFilePath = convertFilePath;
    }

    public void setConvertBusinessName(String convertBusinessName) {
        this.convertBusinessName = convertBusinessName;
    }

    public void writeLog(String content) throws Exception {
        System.out.println(content);
    }
}

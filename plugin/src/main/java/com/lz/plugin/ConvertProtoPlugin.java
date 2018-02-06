package com.lz.plugin;


import com.lz.plugin.task.ConverProtoTask;
import com.lz.plugin.task.RefactoringProtoSrcTask;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

/**
 * Created by xiaoyulaoshi on 2018/1/26.
 */

public class ConvertProtoPlugin implements Plugin<Project> {

    private Project project;


    @Override
    public void apply(Project project) {
        this.project = project;

        File buildFile = project.getProjectDir();

        RefactoringProtoSrcTask task = project.getTasks().create("downloadProto", RefactoringProtoSrcTask.class);
        task.setSourceDirectoryPath(buildFile.getPath() + "\\proto_");
        task.setTargetDirectoryPath(buildFile.getPath() + "\\proto");
        task.setGroup("jsj");


        ConverProtoTask task2 = project.getTasks().create("convert_Proto", ConverProtoTask.class);
        task2.setTargetDirectoryPath(buildFile.getPath() + "\\proto");
        task2.setConvertFilePath(buildFile.getParentFile());
        task2.setGroup("jsj");
        System.out.println("he he  The root directory for the plug-in to run." + buildFile);

    }

}
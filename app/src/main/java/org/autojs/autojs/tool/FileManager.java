package org.autojs.autojs.tool;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    // 获取指定目录下的文件列表
    public List<File> getFilesFromDirectory(String directoryPath) {
        List<File> fileList = new ArrayList<>();
        File directory = new File(context.getExternalFilesDir(null), directoryPath); // 指定目录
        File[] files = directory.listFiles(); // 获取文件列表
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }
}

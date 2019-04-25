package org.octabyte.zeem.Utils;

import com.google.api.client.util.Key;

public class InformerItem {

    @Key
    private String filePath;
    @Key
    private String fileName;

    public InformerItem(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public InformerItem() {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
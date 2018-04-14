package com.isidoreofseville.androidclassroom;

/**
 * Created by Dave on 3/4/2018.
 */

public class FileUploaded {
    private String fileName;
    private String downloadUri;
    private String key;
    private boolean isUploaded;
    private boolean isImage;
    private boolean isFile;


    public FileUploaded(String fileName) {
        this.fileName = fileName;
        this.isUploaded = false;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }
}

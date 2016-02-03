package me.peiwo.peiwo.model;

import java.util.List;

public class AblumImageModel {
    public String topImagePath; // 第一张照片的路径
    public String folderName; // 文件夹名称

    public AblumImageModel(String topImagePath, String folderName) {
        this.topImagePath = topImagePath;
        this.folderName = folderName;
    }

    public AblumImageModel() {
    }

    public List<String> childs;

    public String getTopImagePath() {
        return topImagePath;
    }

    public void setTopImagePath(String topImagePath) {
        this.topImagePath = topImagePath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }


}

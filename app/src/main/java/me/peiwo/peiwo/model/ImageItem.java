package me.peiwo.peiwo.model;

import android.os.Parcel;
import android.os.Parcelable;
import me.peiwo.peiwo.net.PWUploader;

/**
 * 图片对象
 */
public class ImageItem extends PPBaseModel implements Parcelable {
    /*****
     * 七牛的token key
     ********/
    public String qn_token;
    public String qn_key;
    public String qn_thumbnail_url;
    public String qn_url;
    /*************/
    public int imageId = -1;
    public String thumbnailPath;
    public String sourcePath;
    public String imageKey;

    public int width;
    public int height;

    public ImageItem(String thumbnailPath, String sourcePath, int width, int height) {
        this.thumbnailPath = thumbnailPath;
        this.sourcePath = sourcePath;
        this.width = width;
        this.height = height;
    }

    public ImageItem() {

    }


    private boolean selected = false;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setUid(int uid) {
        imageKey = PWUploader.getInstance().getKey(uid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.imageId);
        dest.writeString(this.thumbnailPath);
        dest.writeString(this.sourcePath);
        dest.writeString(this.imageKey);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeByte(selected ? (byte) 1 : (byte) 0);
    }

    protected ImageItem(Parcel in) {
        this.imageId = in.readInt();
        this.thumbnailPath = in.readString();
        this.sourcePath = in.readString();
        this.imageKey = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.selected = in.readByte() != 0;
    }

    public static final Parcelable.Creator<ImageItem> CREATOR = new Parcelable.Creator<ImageItem>() {
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}

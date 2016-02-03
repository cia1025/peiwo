package me.peiwo.peiwo.model;

import java.io.File;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONObject;

import android.net.Uri;

/**
 * Created by Dong Fuhai on 2014-07-21 16:12.
 *
 * @modify:
 */
public class ImageModel extends PPBaseModel implements Parcelable {

//    image_url: "http://static.peiwo.me/image/45/3242596097464204808.jpg",
//                    thumbnail_url: "http://static.peiwo.me/image/45/3242596097464204809.jpg",
//                name: "3242596097464204808"

    public String image_url;
    public String thumbnail_url;
    public String name;

    public ImageModel(String image_url) {
        this.image_url = image_url;
    }

    public ImageModel(JSONObject o) {
        image_url = getJsonValue(o, "image_url");
        thumbnail_url = getJsonValue(o, "thumbnail_url");
        name = getJsonValue(o, "name");
    }

    //public String imgKey;
    public String uploadpath; //待上传的图片地址

    public ImageModel(String localpath, String imgKey) {
        uploadpath = localpath;
        thumbnail_url = Uri.fromFile(new File(localpath)).toString();
        this.name = imgKey;
    }

    public ImageModel(ImageModel e) {
        image_url = e.image_url;
        thumbnail_url = e.thumbnail_url;
        name = e.name;
    }
    public ImageModel() {
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.image_url);
        dest.writeString(this.thumbnail_url);
        dest.writeString(this.name);
        dest.writeString(this.uploadpath);
    }

    protected ImageModel(Parcel in) {
        this.image_url = in.readString();
        this.thumbnail_url = in.readString();
        this.name = in.readString();
        this.uploadpath = in.readString();
    }

    public static final Parcelable.Creator<ImageModel> CREATOR = new Parcelable.Creator<ImageModel>() {
        public ImageModel createFromParcel(Parcel source) {
            return new ImageModel(source);
        }

        public ImageModel[] newArray(int size) {
            return new ImageModel[size];
        }
    };
}

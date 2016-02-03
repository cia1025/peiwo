package me.peiwo.peiwo.model;

import android.os.Parcel;
import android.text.TextUtils;
import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * Created by fuhaidong on 15/12/9.
 */
@MessageTag(value = "RC:PWMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class GroupMessageModel extends MessageContent {
    private String body;

    public GroupMessageModel(byte[] data) {
        super(data);
        if (data != null) {
            body = new String(data);
        }
    }

    public GroupMessageModel() {
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public GroupMessageModel(String body) {
        this.body = body;
    }

    public GroupMessageModel(Parcel in) {
        body = ParcelUtils.readFromParcel(in);
    }

    @Override
    public byte[] encode() {
        if (!TextUtils.isEmpty(body)) {
            return body.getBytes();
        }
        return new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, body);
    }

    public static final Creator<GroupMessageModel> CREATOR = new Creator<GroupMessageModel>() {
        @Override
        public GroupMessageModel createFromParcel(Parcel source) {
            return new GroupMessageModel(source);
        }

        @Override
        public GroupMessageModel[] newArray(int size) {
            return new GroupMessageModel[size];
        }
    };
}

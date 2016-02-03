package me.peiwo.peiwo.model;

import android.os.Parcel;
import android.os.Parcelable;
import me.peiwo.peiwo.util.CustomLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dong Fuhai on 2014-07-24 12:27.
 *
 * @modify:
 */
public class PWUserModel extends PPBaseModel implements Parcelable {

    public int uid;

    public String birthday;
    public String avatar_thumbnail;
    public String profession;
    public int state;
    public String avatar;
    public String city;
    public int emotion;
    public String slogan;
    public String price;
    public String money;
    public String name;
    public String province;
    public String session_data;
    public int gender;
    public long call_duration;
    public int permission;
    public String phone;
    public String images_str;
    public int impermission;
    public boolean paid;

    public int flags;
    public int relation;
    public ArrayList<String> dynamicList;
    public String dynamicContent;

    public double complement;

    public int focusesNumber;
    public int fansNumber;

    public String tags;
    public String food_tags;
    public String music_tags;
    public String movie_tags;
    public String book_tags;
    public String travel_tags;
    public String sport_tags;
    public String game_tags;
    public int reward_price;
    public boolean has_lazy_voice;
    public boolean isCharge;

    public String score;

    public String remark; // 备注

    public ArrayList<ImageModel> images = new ArrayList<ImageModel>();
    public ArrayList<Integer> xzList = null;

    public PWUserModel(JSONObject o) {
        try {
            uid = getJsonInt(o, "uid");
            state = getJsonInt(o, "state");
            name = getJsonValue(o, "name");
            reward_price = getJsonInt(o, "reward_price");
            avatar = getJsonValue(o, "avatar");
            images_str = o.has("images") ? o.getJSONArray("images").toString() : "";
            setImages(o);
            gender = getJsonInt(o, "gender");
            birthday = getJsonValue(o, "birthday");

            province = getJsonValue(o, "province");
            city = getJsonValue(o, "city");
            slogan = getJsonValue(o, "slogan");
            emotion = getJsonInt(o, "emotion");
            tags = getTags(o, "tags");
            food_tags = getTags(o, "food_tags");
            music_tags = getTags(o, "music_tags");
            movie_tags = getTags(o, "movie_tags");
            book_tags = getTags(o, "book_tags");
            travel_tags = getTags(o, "travel_tags");
            sport_tags = getTags(o, "sport_tags");
            game_tags = getTags(o, "app_tags");

            double dPrice = o.has("price") ? o.getDouble("price") : 0;
            price = dPrice == 0 ? "0" : getPrice(dPrice);
            isCharge = !price.equals("0");
            has_lazy_voice = o.has("voice");

            call_duration = o.has("call_duration") ? o.getLong("call_duration") : 0;
            flags = getJsonInt(o, "flags");
            complement = o.has("complement") ? o.optDouble("complement") : -1;
            permission = getJsonInt(o, "permission");
            impermission = getJsonInt(o, "im_permission");
            phone = getJsonValue(o, "phone");
            session_data = getJsonValue(o, "session_data");
            money = getJsonValue(o, "money");
            focusesNumber = getJsonInt(o, "focuses");
            fansNumber = getJsonInt(o, "fans");
            relation = o.has("relation") ? getJsonInt(o, "relation") : -1;
            score = getJsonValue(o, "score");
            setTrendImage(o);
            setHpXZ(o);
            profession = getJsonValue(o, "profession");
            avatar_thumbnail = getJsonValue(o, "avatar_thumbnail");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTrendImage(JSONObject o) throws JSONException {
        JSONArray array = getJsonArray(o, "feeds");

        if (array != null && array.length() > 0) {
            dynamicList = new ArrayList<String>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject dynamicJSON = array.optJSONObject(i);
                if (dynamicJSON == null) {
                    continue;
                }
                if (dynamicJSON.has("thumbnail_url")) {
                    String url = dynamicJSON.optString("thumbnail_url");
                    dynamicList.add(url);
                }
                if (dynamicJSON.has("content")) {
                    dynamicContent = dynamicJSON.optString("content");
                }
            }
        }
    }

    private void setHpXZ(JSONObject o) throws JSONException {
        JSONArray array = getJsonArray(o, "hepai_xingzuo");

        if (array != null && array.length() > 0) {
            xzList = new ArrayList<Integer>();
            for (int i = 0; i < array.length(); i++) {
                xzList.add(array.optInt(i));
            }
        }
    }

    private void setImages(JSONObject o) throws JSONException {
        JSONArray array = o.has("images") ? o.getJSONArray("images") : null;
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                images.add(new ImageModel(array.getJSONObject(i)));
            }
        }
    }

    private String getPrice(double price) {
        return String.valueOf(Float.valueOf(new DecimalFormat("#.0").format(price)));
    }

    public float getPriceFloat() {
        return Float.valueOf(price);
    }

    private String getTags(JSONObject o, String tagName) throws JSONException {
        if (o.has(tagName)) {
            JSONArray tagsArray = o.getJSONArray(tagName);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tagsArray.length(); i++) {
                sb.append(tagsArray.getString(i));
                if (i != tagsArray.length() - 1)
                    sb.append(",");
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    public PWUserModel() {

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.uid);
        dest.writeString(this.birthday);
        dest.writeString(this.avatar_thumbnail);
        dest.writeString(this.profession);
        dest.writeInt(this.state);
        dest.writeString(this.avatar);
        dest.writeString(this.city);
        dest.writeInt(this.emotion);
        dest.writeString(this.slogan);
        dest.writeString(this.price);
        dest.writeString(this.money);
        dest.writeString(this.name);
        dest.writeString(this.province);
        dest.writeString(this.session_data);
        dest.writeInt(this.gender);
        dest.writeLong(this.call_duration);
        dest.writeInt(this.permission);
        dest.writeString(this.phone);
        dest.writeString(this.images_str);
        dest.writeInt(this.impermission);
        dest.writeInt(this.flags);
        dest.writeInt(this.relation);
        dest.writeStringList(this.dynamicList);
        dest.writeString(this.dynamicContent);
        dest.writeDouble(this.complement);
        dest.writeInt(this.focusesNumber);
        dest.writeInt(this.fansNumber);
        dest.writeString(this.tags);
        dest.writeString(this.food_tags);
        dest.writeString(this.music_tags);
        dest.writeString(this.movie_tags);
        dest.writeString(this.book_tags);
        dest.writeString(this.travel_tags);
        dest.writeString(this.sport_tags);
        dest.writeString(this.game_tags);
        dest.writeInt(this.reward_price);
        dest.writeString(this.score);
        dest.writeString(this.remark);
        dest.writeTypedList(images);
        dest.writeList(this.xzList);
    }

    protected PWUserModel(Parcel in) {
        this.uid = in.readInt();
        this.birthday = in.readString();
        this.avatar_thumbnail = in.readString();
        this.profession = in.readString();
        this.state = in.readInt();
        this.avatar = in.readString();
        this.city = in.readString();
        this.emotion = in.readInt();
        this.slogan = in.readString();
        this.price = in.readString();
        this.money = in.readString();
        this.name = in.readString();
        this.province = in.readString();
        this.session_data = in.readString();
        this.gender = in.readInt();
        this.call_duration = in.readLong();
        this.permission = in.readInt();
        this.phone = in.readString();
        this.images_str = in.readString();
        this.impermission = in.readInt();
        this.flags = in.readInt();
        this.relation = in.readInt();
        this.dynamicList = in.createStringArrayList();
        this.dynamicContent = in.readString();
        this.complement = in.readDouble();
        this.focusesNumber = in.readInt();
        this.fansNumber = in.readInt();
        this.tags = in.readString();
        this.food_tags = in.readString();
        this.music_tags = in.readString();
        this.movie_tags = in.readString();
        this.book_tags = in.readString();
        this.travel_tags = in.readString();
        this.sport_tags = in.readString();
        this.game_tags = in.readString();
        this.reward_price = in.readInt();
        this.score = in.readString();
        this.remark = in.readString();
        this.images = in.createTypedArrayList(ImageModel.CREATOR);
        this.xzList = new ArrayList<Integer>();
        in.readList(this.xzList, List.class.getClassLoader());
    }

    public static final Parcelable.Creator<PWUserModel> CREATOR = new Parcelable.Creator<PWUserModel>() {
        public PWUserModel createFromParcel(Parcel source) {
            return new PWUserModel(source);
        }

        public PWUserModel[] newArray(int size) {
            return new PWUserModel[size];
        }
    };
}


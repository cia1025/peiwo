package me.peiwo.peiwo.model;

import android.text.TextUtils;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.UserManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FeedFlowModel extends PPBaseModel {
    public int view_type = 0;

    private long create_time;
    private String update_time;
    public String prev_time;
    private int like_number;
    /**
     * 取消点赞/点赞
     */
    private int is_like;
    private String id;

    private int is_top = 0;

    private String content;

    public PWUserModel userModel;

    private int topicId;
    private String topicContent;
    private boolean isMy;
    private String location;
    private ArrayList<ImageModel> imageList = null;
    private ArrayList<FeedFlowLikersModel> likerList = null;

    public FeedFlowModel() {
    }


    public int imageWidth = 0;
    public int imageHeight = 0;

    public FeedFlowModel(JSONObject dataJson) {
        if (dataJson == null)
            return;
        CustomLog.d("FeedFlowModel. dataJson is : "+dataJson);
        create_time = dataJson.optLong("create_time") * 1000;
        content = dataJson.optString("content");
        update_time = dataJson.optString("update_time");
        prev_time = dataJson.optString("update_time");
        id = dataJson.optString("id");
        like_number = dataJson.optInt("like_number");
        location = dataJson.optString("location");
        is_like = dataJson.optInt("is_like");
        is_top = dataJson.optInt("is_top");

        JSONObject topicJson = dataJson.optJSONObject("topic");
        if (topicJson != null) {
            topicId = topicJson.optInt("id");
            topicContent = topicJson.optString("content");
        }
        JSONObject userJson = dataJson.optJSONObject("user");
        if (userJson != null) {
            userModel = new PWUserModel(userJson);
        } else {
            userModel = new PWUserModel();
        }
        JSONObject extraJson = dataJson.optJSONObject("extra");
        if (extraJson != null) {
            JSONArray imageArray = extraJson.optJSONArray("images");
            if (imageArray != null && imageArray.length() > 0) {
                imageList = new ArrayList<ImageModel>();
                for (int i = 0; i < imageArray.length(); i++) {
                    JSONObject imageObject = imageArray.optJSONObject(i);
                    if (imageObject != null) {
                        ImageModel imageModel = new ImageModel(imageObject);
                        imageList.add(imageModel);
                    }
                }
            }
            imageWidth = extraJson.optInt("width");
            imageHeight = extraJson.optInt("height");
        }

        JSONArray likerArray = dataJson.optJSONArray("likers");
        if (likerArray != null && likerArray.length() > 0) {
            likerList = new ArrayList<FeedFlowLikersModel>();

            for (int i = 0; i < likerArray.length(); i++) {
                JSONObject likerObject = likerArray.optJSONObject(i);
                if (likerObject != null) {
                    FeedFlowLikersModel likerModel = new FeedFlowLikersModel();
                    likerModel.uid = likerObject.optInt("uid");
                    likerModel.avatar = likerObject.optString("avatar");
                    likerList.add(likerModel);
                }
            }
        }
        if (userModel.uid == UserManager.getUid(PeiwoApp.getApplication())) {
            isMy = true;
        } else {
            isMy = false;
        }
    }

    public int getView_type() {
        return view_type;
    }

    public long getCreate_time() {
        return create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public int getLike_number() {
        return like_number;
    }

    public void setLike_number(int like_number) {
        this.like_number = like_number;
    }


    public String getId() {
        if (TextUtils.isEmpty(id)) {
            return "";
        }
        return id;
    }

    public String getContent() {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        return content;
    }

    public String getLocation() {
        return location;
    }

    public int getTopicId() {
        return topicId;
    }

    public String getTopicContent() {
        if (TextUtils.isEmpty(topicContent)) {
            return "";
        }
        return topicContent;
    }

    public int getIs_like() {
        return is_like;
    }

    public void setIs_like(int is_like) {
        this.is_like = is_like;
    }

    public PWUserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(PWUserModel userModel) {
        this.userModel = userModel;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public void setTopicContent(String topicContent) {
        this.topicContent = topicContent;
    }

    public void setMy(boolean isMy) {
        this.isMy = isMy;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImageList(ArrayList<ImageModel> imageList) {
        this.imageList = imageList;
    }

    public void setLikerList(ArrayList<FeedFlowLikersModel> likerList) {
        this.likerList = likerList;
    }

    public boolean isMy() {
        return isMy;
    }

    public int getIs_top() {
        return is_top;
    }

    public ArrayList<ImageModel> getImageList() {
        return imageList;
    }

    public ArrayList<FeedFlowLikersModel> getLikerList() {
        return likerList;
    }

    public static class FeedFlowLikersModel {
        public int uid;
        public String avatar;
    }
}

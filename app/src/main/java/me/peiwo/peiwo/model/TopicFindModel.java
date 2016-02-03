package me.peiwo.peiwo.model;


import org.json.JSONObject;

public class TopicFindModel extends PPBaseModel {
    private static final long serialVersionUID = 1L;

    public String view_count;
    public String subtitle;
    public String state;
    public String content;
    public String creator_id;
    public String create_time;
    public String real_count;
    public String id;

    public TopicFindModel(JSONObject object) {
        view_count = getJsonValue(object, "view_count");
        subtitle = getJsonValue(object, "subtitle");
        state = getJsonValue(object, "state");
        content = getJsonValue(object, "content");
        creator_id = getJsonValue(object, "creator_id");
        create_time = getJsonValue(object, "create_time");
        real_count = getJsonValue(object, "real_count");
        id = getJsonValue(object, "id");
    }
}

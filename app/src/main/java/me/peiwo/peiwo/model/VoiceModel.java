package me.peiwo.peiwo.model;

import me.peiwo.peiwo.adapter.TabFindAdapter;
import me.peiwo.peiwo.util.CustomLog;
import org.json.JSONObject;

/**
 * Created by gaoxiang on 2015/12/1.
 */
public class VoiceModel {
    public static final int PLAY_STATUS_PLAYING = 1;
    public static final int PLAY_STATUS_PAUSE = 2;
    public static final int PLAY_STATUS_IDLE = 3;
    public static final int PLAY_STATUS_RESUME = 4;
    public static final int PLAY_STATUS_LOADING = 5;

    public VoiceModel() {

    }
    public VoiceModel(JSONObject o) {
        CustomLog.d("voice json is : "+o.toString());
        this.md5_code = o.optString("md5_code");
        this.voice_url = o.optString("voice_url");
        this.length = o.optInt("length");
        this.filename = o.optString("filename");
        this.voice_key = o.optString("key");
    }

    public int play_status = PLAY_STATUS_IDLE;
    public String md5_code;
    public String voice_url;
    public int length;
    public String filename;
    public String voice_key;
}